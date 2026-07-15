package com.carlosarancibia.playfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.fold
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductGameStateTransitions
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductOnboardingRules
import com.carlosarancibia.playfit.model.ProductOnboardingValidation
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductPlayStatus
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.ThemeMode
import com.carlosarancibia.playfit.model.fallbackPlatforms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.Instant

data class PlayfitUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val toast: String? = null,
    val toastActionLabel: String? = null,
    val showingStaleData: Boolean = false,
    val pendingSync: Boolean = false,
)

private data class PendingDecisionUndo(
    val gameId: String,
    val previousState: ProductGameState?,
    val previousProductState: ProductState,
    val previousPlayNext: ProductPlayNextModel?,
    val previousPicks: List<RankedSeedGame>,
    val previousExcludedIds: Set<String>,
)

data class PlatformsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val showingStaleData: Boolean = false,
)

data class SearchUiState(
    val query: String = "",
    val selectedFamily: String? = null,
    val results: List<SeedGame> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = results.size < total
}

internal fun mergeSearchResults(existing: List<SeedGame>, incoming: List<SeedGame>): List<SeedGame> {
    val seen = existing.mapTo(mutableSetOf()) { it.gameId }
    return existing + incoming.filterNot { it.gameId in seen }
}

sealed interface DossierUiState {
    data object Idle : DossierUiState
    data class Loading(val gameId: String) : DossierUiState
    data class Success(val entry: RankedSeedGame) : DossierUiState
    data class NotFound(val gameId: String) : DossierUiState
    data class Error(val gameId: String, val message: String) : DossierUiState
}

@HiltViewModel
class PlayfitViewModel @Inject constructor(
    private val repository: PlayfitRepository,
    val authManager: AuthManager,
    val preferencesDataStore: PreferencesDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    private val _ui = MutableStateFlow(PlayfitUiState())
    val ui: StateFlow<PlayfitUiState> = _ui.asStateFlow()

    private val authCoordinator = AuthCoordinator(
        authManager = authManager,
        scope = viewModelScope,
        setToast = ::setToast,
        setError = { message -> _ui.value = _ui.value.copy(error = message) },
    )
    val authState: StateFlow<AuthState> = authCoordinator.state
    val pendingPasswordRecovery = authCoordinator.pendingPasswordRecovery

    private val playNextQueueCoordinator = PlayNextQueueCoordinator()
    private val initialDataCoordinator = InitialDataCoordinator(
        repository = repository,
        preferencesDataStore = preferencesDataStore,
    )

    private val _playNext = MutableStateFlow<ProductPlayNextModel?>(null)
    val playNext: StateFlow<ProductPlayNextModel?> = _playNext.asStateFlow()

    private val _picks = MutableStateFlow<List<RankedSeedGame>>(emptyList())
    val picks: StateFlow<List<RankedSeedGame>> = _picks.asStateFlow()

    private val _tasteModel = MutableStateFlow<ProductTasteModel?>(null)
    val tasteModel: StateFlow<ProductTasteModel?> = _tasteModel.asStateFlow()

    private val _platforms = MutableStateFlow(fallbackPlatforms)
    val platforms: StateFlow<List<Platform>> = _platforms.asStateFlow()

    private val _platformsUi = MutableStateFlow(PlatformsUiState())
    val platformsUi: StateFlow<PlatformsUiState> = _platformsUi.asStateFlow()

    private val _dossier = MutableStateFlow<DossierUiState>(DossierUiState.Idle)
    val dossier: StateFlow<DossierUiState> = _dossier.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()
    private var searchJob: Job? = null
    private var searchRequestSeq = 0L

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _excludedIds = MutableStateFlow<Set<String>>(emptySet())
    val excludedIds: StateFlow<Set<String>> = _excludedIds.asStateFlow()

    private var pendingDecisionUndo: PendingDecisionUndo? = null

    private val _selectedPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPlatformIds: StateFlow<Set<String>> = _selectedPlatformIds.asStateFlow()

    // Onboarding temporary states (survive layout changes / recreation)
    private val _onboardingStep = MutableStateFlow(0)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _onboardingSelectedPlatforms = MutableStateFlow(setOf<String>())
    val onboardingSelectedPlatforms: StateFlow<Set<String>> = _onboardingSelectedPlatforms.asStateFlow()

    private val _onboardingLikedGames = MutableStateFlow(listOf<SeedGame?>(null, null, null))
    val onboardingLikedGames: StateFlow<List<SeedGame?>> = _onboardingLikedGames.asStateFlow()

    private val _onboardingDislikedGames = MutableStateFlow(listOf<SeedGame?>(null))
    val onboardingDislikedGames: StateFlow<List<SeedGame?>> = _onboardingDislikedGames.asStateFlow()

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun setOnboardingSelectedPlatforms(ids: Set<String>) {
        _onboardingSelectedPlatforms.value = ids
    }

    fun setOnboardingLikedGames(games: List<SeedGame?>) {
        _onboardingLikedGames.value = games
    }

    fun setOnboardingDislikedGames(games: List<SeedGame?>) {
        _onboardingDislikedGames.value = games
    }

    fun resetOnboardingState() {
        _onboardingStep.value = 0
        _onboardingSelectedPlatforms.value = emptySet()
        _onboardingLikedGames.value = listOf(null, null, null)
        _onboardingDislikedGames.value = listOf(null)
    }

    private var saveJob: Job? = null
    private var platformSaveJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesDataStore.selectedPlatformIds.collect { ids ->
                _selectedPlatformIds.value = ids
            }
        }
        viewModelScope.launch {
            preferencesDataStore.onboardingCompleted.collect { completed ->
                _onboardingCompleted.value = completed
            }
        }
        viewModelScope.launch {
            preferencesDataStore.themeMode.collect { mode ->
                _themeMode.value = ThemeMode.fromApiValue(mode)
            }
        }
        authCoordinator.observeSession()
        viewModelScope.launch {
            repository.observePendingSync().collect { pending ->
                _ui.value = _ui.value.copy(pendingSync = pending)
            }
        }
        viewModelScope.launch {
            initializeSessionAndData()
        }
    }

    private suspend fun initializeSessionAndData() {
        authCoordinator.restoreOrCreateAnonymousSession()
        loadInitialData()
    }

    private suspend fun loadInitialData() {
        _ui.value = _ui.value.copy(loading = true)
        _platformsUi.value = _platformsUi.value.copy(loading = true, error = null)
        val snapshot = initialDataCoordinator.load()
        applyInitialDataSnapshot(snapshot)
    }

    /**
     * Recommendations aren't computed synchronously by the backend when onboarding
     * completes, so the first fetch can come back empty (200, no error) before the
     * server has caught up. Retries a few times, keeping `loading = true` throughout
     * so Play Next stays on its existing "still checking" skeleton instead of ever
     * reaching the empty state.
     */
    private suspend fun loadInitialDataAfterOnboarding() {
        _ui.value = _ui.value.copy(loading = true)
        _platformsUi.value = _platformsUi.value.copy(loading = true, error = null)

        var snapshot = initialDataCoordinator.load()
        var attempt = 0
        while (
            snapshot.error == null &&
            snapshot.playNext.isEmptyRecommendations() &&
            attempt < MAX_ONBOARDING_RECS_ATTEMPTS
        ) {
            delay(ONBOARDING_RECS_RETRY_DELAY_MS)
            snapshot = initialDataCoordinator.load()
            attempt++
        }

        applyInitialDataSnapshot(snapshot)
    }

    private fun applyInitialDataSnapshot(snapshot: InitialDataSnapshot) {
        snapshot.state?.let { _state.value = it }
        snapshot.playNext?.let { _playNext.value = it }
        snapshot.picks?.let { _picks.value = it }
        snapshot.tasteModel?.let { _tasteModel.value = it }
        _platforms.value = snapshot.platforms
        _platformsUi.value = snapshot.platformsUi
        _ui.value = _ui.value.copy(
            loading = false,
            error = snapshot.error,
            showingStaleData = snapshot.showingStaleData,
        )
    }

    private fun ProductPlayNextModel?.isEmptyRecommendations(): Boolean =
        this == null || (primary == null && alternatives.isEmpty())

    fun linkGoogleAccount() {
        authCoordinator.linkGoogleAccount()
    }

    fun signOutAsync() {
        authCoordinator.signOutAsync()
    }

    fun deleteAccount() {
        if (!authState.value.canDeleteAccount) {
            setToast("Sign in before deleting your cloud profile.")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            safeRepositoryCall { repository.deleteCloudProfile() }.fold(
                onFailure = { error ->
                    _ui.value = _ui.value.copy(saving = false, error = error.message)
                    setToast(error.message)
                },
                onSuccess = {
                    clearPlayfitDataAfterProfileDeletion()
                    when (val signOut = authCoordinator.signOut()) {
                        is AuthResult.Error -> {
                            _ui.value = _ui.value.copy(error = signOut.message)
                            setToast("Cloud profile deleted, but sign out failed: ${signOut.message}")
                        }
                        is AuthResult.Pending -> {
                            setToast("Cloud profile deleted. ${signOut.message}")
                        }
                        is AuthResult.Success -> {
                            setToast("Cloud profile deleted.")
                        }
                    }
                },
            )
        }
    }

    suspend fun signInAnonymously(): AuthResult = authCoordinator.signInAnonymously()

    suspend fun signInWithGoogle(): AuthResult = authCoordinator.signInWithGoogle()

    suspend fun signInWithEmail(email: String, password: String): AuthResult =
        authCoordinator.signInWithEmail(email, password)

    suspend fun signUpWithEmail(email: String, password: String): AuthResult =
        authCoordinator.signUpWithEmail(email, password)

    suspend fun resetPassword(email: String): AuthResult = authCoordinator.resetPassword(email)

    suspend fun signInAsGuest(): AuthResult = authCoordinator.signInAsGuest()

    suspend fun signOut(): AuthResult = authCoordinator.signOut()

    suspend fun updatePassword(newPassword: String): AuthResult =
        authCoordinator.updatePassword(newPassword)

    fun cancelPendingPasswordRecovery() = authCoordinator.cancelPendingPasswordRecovery()

    fun completeOnboarding(draft: ProductOnboardingDraft? = null) {
        viewModelScope.launch {
            val completedDraft = draft ?: run {
                _ui.value = _ui.value.copy(error = "Complete calibration before continuing.")
                return@launch
            }
            when (val validation = ProductOnboardingRules.validate(completedDraft)) {
                is ProductOnboardingValidation.Invalid -> {
                    _ui.value = _ui.value.copy(error = validation.message)
                    return@launch
                }
                ProductOnboardingValidation.Valid -> Unit
            }
            val completedAt = Instant.now().toString()
            _state.value = _state.value.copy(
                user = _state.value.user.copy(
                    onboarding = completedDraft,
                    onboardingCompletedAt = completedAt,
                ),
            )
            preferencesDataStore.setSelectedPlatformIds(
                completedDraft.platforms.map { it.platformId }.toSet(),
            )
            safeRepositoryCall { repository.saveOnboarding(completedDraft, completedAt) }.fold(
                onFailure = { error ->
                    _ui.value = _ui.value.copy(error = error.message)
                    return@launch
                },
                onSuccess = { result ->
                    _ui.value = _ui.value.copy(pendingSync = result.pendingSync)
                },
            )
            preferencesDataStore.setOnboardingCompleted(true)
            _onboardingCompleted.value = true
            loadInitialDataAfterOnboarding()
            setToast("Profile ready. Your Play Next pick is ready.")
        }
    }

    fun updatePlatforms(ids: Set<String>) {
        if (ids.isEmpty()) {
            setToast("Keep at least one active platform.")
            return
        }
        val draft = _state.value.user.onboarding.copy(
            platforms = ids.sorted().map {
                com.carlosarancibia.playfit.model.ProductPlatformSelection(
                    platformId = it,
                    status = com.carlosarancibia.playfit.model.ProductAccessStatus.Available,
                )
            },
        )
        when (val validation = ProductOnboardingRules.validate(draft)) {
            is ProductOnboardingValidation.Invalid -> {
                setToast(validation.message)
                return
            }
            ProductOnboardingValidation.Valid -> Unit
        }
        _state.value = _state.value.copy(user = _state.value.user.copy(onboarding = draft))
        viewModelScope.launch {
            preferencesDataStore.setSelectedPlatformIds(ids)
        }
        platformSaveJob?.cancel()
        platformSaveJob = viewModelScope.launch {
            delay(500)
            safeRepositoryCall { repository.saveOnboarding(draft, _state.value.user.onboardingCompletedAt) }.fold(
                onFailure = { error -> _ui.value = _ui.value.copy(error = error.message) },
                onSuccess = { result ->
                    _ui.value = _ui.value.copy(pendingSync = result.pendingSync)
                    setToast(if (result.pendingSync) "Platforms saved; waiting to sync" else "Platforms updated")
                    if (!result.pendingSync) refreshRecommendations()
                },
            )
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(refreshing = true)
            val previousStateVersion = _playNext.value?.stateVersion
            safeRepositoryCall { repository.refreshRecommendations() }.fold(
                onFailure = { error ->
                    _ui.value = _ui.value.copy(refreshing = false, error = error.message)
                },
                onSuccess = { result ->
                    _playNext.value = result.data
                    _ui.value = _ui.value.copy(
                        refreshing = false,
                        showingStaleData = result.isStale,
                        error = null,
                    )
                    refreshSecondaryData(includeState = previousStateVersion != result.data.stateVersion)
                    setToast(if (result.isStale) "Showing saved recommendations" else "Recommendations updated")
                },
            )
        }
    }

    fun getGameRecommendation(gameId: String): RankedSeedGame? {
        val fromPlayNext = _playNext.value?.let { model ->
            model.primary?.takeIf { it.game.gameId == gameId }
                ?: model.alternatives.firstOrNull { it.game.gameId == gameId }
        }
        if (fromPlayNext != null) return fromPlayNext
        return _picks.value.firstOrNull { it.game.gameId == gameId }
    }

    fun loadGameRecommendation(gameId: String, forceRefresh: Boolean = false) {
        if (gameId.isBlank()) {
            _dossier.value = DossierUiState.NotFound(gameId)
            return
        }
        val local = getGameRecommendation(gameId)
        if (local != null && !forceRefresh) {
            _dossier.value = DossierUiState.Success(local)
            viewModelScope.launch {
                val result = safeRepositoryCall { repository.getGameRecommendation(gameId) }
                if (result is RepositoryResult.Success && result.data != null) {
                    _dossier.value = DossierUiState.Success(result.data)
                }
            }
            return
        }
        viewModelScope.launch {
            _dossier.value = DossierUiState.Loading(gameId)
            safeRepositoryCall { repository.getGameRecommendation(gameId) }.fold(
                onFailure = { error -> _dossier.value = DossierUiState.Error(gameId, error.message) },
                onSuccess = { result ->
                    _dossier.value = result.data
                        ?.let(DossierUiState::Success)
                        ?: DossierUiState.NotFound(gameId)
                },
            )
        }
    }

    fun togglePick(gameId: String) {
        viewModelScope.launch {
            val current = _picks.value.any { it.game.gameId == gameId }
            val recommendation = getGameRecommendation(gameId)
            val existing = _state.value.user.gameStates[gameId]
            if (!current && (existing?.excluded == true || existing?.status.isTerminal())) {
                setToast("Finished or excluded games cannot be added to Picks")
                return@launch
            }
            if (!current && _picks.value.size >= 100) {
                setToast("Picks is limited to 100 games")
                return@launch
            }
            _ui.value = _ui.value.copy(saving = true, error = null)
            val write = try {
                repository.togglePick(gameId, !current)
            } catch (error: Exception) {
                _ui.value = _ui.value.copy(saving = false)
                setToast(error.message ?: "Could not update pick")
                return@launch
            }
            if (write is RepositoryResult.Failure) {
                _ui.value = _ui.value.copy(saving = false)
                setToast(write.error.message)
                return@launch
            }
            if (write is RepositoryResult.Success) {
                _ui.value = _ui.value.copy(
                    saving = false,
                    pendingSync = _ui.value.pendingSync || write.pendingSync,
                )
            }
            _picks.value = if (current) {
                _picks.value.filterNot { it.game.gameId == gameId }
            } else {
                recommendation?.let { listOf(it.copy(inPlayfitPicks = true)) + _picks.value }
                    ?: _picks.value
            }
            updateSavedPickIds(gameId = gameId, picked = !current)
            val next = ProductGameStateTransitions.setPick(existing, gameId, picked = !current)
            updateGameState(next)
            if (!current) {
                removeRecommendation(gameId)
                refreshRecommendationsAfterActionIfNeeded()
            }
            if (current) setToast("Removed from picks") else setToast("Saved to picks")
        }
    }

    fun skipRecommendation(gameId: String) {
        _excludedIds.value = _excludedIds.value + gameId
        removeRecommendation(gameId)
        setToast("Skipped")
    }

    fun clearSkipped() {
        _excludedIds.value = emptySet()
        refreshRecommendations()
    }

    suspend fun searchGames(query: String): List<SeedGame> {
        return safeRepositoryCall { repository.searchGames(query) }.fold(
            onSuccess = { it.data.games },
            onFailure = { error ->
                _ui.value = _ui.value.copy(error = error.message)
                throw IllegalStateException(error.message)
            },
        )
    }

    fun updateSearchQuery(query: String) {
        _search.value = _search.value.copy(query = query)
        triggerSearch(resetPage = true)
    }

    fun updateSearchFamily(family: String?) {
        val next = if (_search.value.selectedFamily == family) null else family
        _search.value = _search.value.copy(selectedFamily = next)
        triggerSearch(resetPage = true)
    }

    fun loadMoreSearchResults() {
        if (_search.value.loadingMore || !_search.value.hasMore) return
        triggerSearch(resetPage = false)
    }

    fun retrySearch() {
        triggerSearch(resetPage = true)
    }

    fun resetSearch() {
        searchJob?.cancel()
        _search.value = SearchUiState()
    }

    private fun triggerSearch(resetPage: Boolean) {
        searchJob?.cancel()
        val state = _search.value
        val nextPage = if (resetPage) 1 else state.page + 1
        val mySeq = ++searchRequestSeq

        searchJob = viewModelScope.launch {
            _search.value = _search.value.copy(
                page = nextPage,
                loading = resetPage,
                loadingMore = !resetPage,
                error = null,
            )
            delay(SEARCH_DEBOUNCE_MS)
            if (mySeq != searchRequestSeq) return@launch

            val platformIds = state.selectedFamily
                ?.let { family -> _platforms.value.filter { it.family == family }.map { it.platformId } }
                .orEmpty()

            val result = safeRepositoryCall {
                repository.searchGames(
                    query = state.query,
                    platformIds = platformIds,
                    page = nextPage,
                    pageSize = SEARCH_PAGE_SIZE,
                )
            }
            if (mySeq != searchRequestSeq) return@launch

            result.fold(
                onSuccess = { success ->
                    val page = success.data
                    _search.value = _search.value.copy(
                        results = if (resetPage) page.games else mergeSearchResults(_search.value.results, page.games),
                        total = page.total,
                        loading = false,
                        loadingMore = false,
                        error = null,
                    )
                },
                onFailure = { error ->
                    _search.value = _search.value.copy(loading = false, loadingMore = false, error = error.message)
                },
            )
        }
    }

    fun loadTasteModel() {
        viewModelScope.launch {
            try {
                repository.getTasteModel().fold(
                    onSuccess = { result ->
                        _tasteModel.value = result.data
                        _ui.value = _ui.value.copy(showingStaleData = result.isStale)
                    },
                    onFailure = { error -> _ui.value = _ui.value.copy(error = error.message) },
                )
            } catch (error: Exception) {
                _ui.value = _ui.value.copy(error = error.message)
            }
        }
    }

    fun removePick(gameId: String) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(saving = true, error = null)
                repository.togglePick(gameId, false).fold(
                    onFailure = { error ->
                        _ui.value = _ui.value.copy(saving = false)
                        setToast(error.message)
                        return@launch
                    },
                    onSuccess = { result ->
                        _ui.value = _ui.value.copy(
                            saving = false,
                            pendingSync = _ui.value.pendingSync || result.pendingSync,
                        )
                    },
                )
                _picks.value = _picks.value.filterNot { it.game.gameId == gameId }
                updateSavedPickIds(gameId = gameId, picked = false)
                val existing = _state.value.user.gameStates[gameId]
                updateGameState(ProductGameStateTransitions.setPick(existing, gameId, picked = false))
                loadTasteModel()
                setToast("Removed from picks.")
            } catch (_: Exception) {
                _ui.value = _ui.value.copy(saving = false)
                setToast("Could not remove pick.")
            }
        }
    }

    fun deleteSignal(gameId: String, source: String) {
        viewModelScope.launch {
            val current = _state.value
            var draft = current.user.onboarding
            var nextGameState = current.user.gameStates[gameId]

            when (source) {
                "onboarding_liked" -> draft = draft.copy(
                    likedGameIds = draft.likedGameIds.filterNot { it == gameId },
                )
                "onboarding_disliked" -> draft = draft.copy(
                    dislikedGameIds = draft.dislikedGameIds.filterNot { it == gameId },
                )
                else -> nextGameState = nextGameState?.let { state ->
                    state.copy(
                        rating = null,
                        excluded = false,
                        status = state.status.takeUnless { it.isTerminal() },
                    )
                }
            }

            if (source.startsWith("onboarding_") && nextGameState?.isInert() == true) {
                nextGameState = null
            }

            val write = if (nextGameState == null || nextGameState.isInert()) {
                safeRepositoryCall { repository.deleteGameState(gameId) }
            } else {
                safeRepositoryCall { repository.saveGameState(gameId, nextGameState) }
            }
            if (write is RepositoryResult.Failure) {
                setToast(write.error.message)
                return@launch
            }

            val gameStates = current.user.gameStates.toMutableMap().apply {
                if (nextGameState == null || nextGameState.isInert()) remove(gameId)
                else put(gameId, nextGameState)
            }
            _state.value = current.copy(
                user = current.user.copy(onboarding = draft, gameStates = gameStates),
            )
            _picks.value = _picks.value.filterNot { it.game.gameId == gameId && nextGameState?.inPlayfitPicks != true }

            safeRepositoryCall { repository.rebuildTasteProfile(draft, current.user.onboardingCompletedAt) }.fold(
                onFailure = { error -> _ui.value = _ui.value.copy(error = error.message) },
                onSuccess = { result ->
                    if (!result.isStale) {
                        _state.value = _state.value.copy(
                            user = _state.value.user.copy(profile = result.data),
                        )
                    }
                    _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || result.pendingSync)
                },
            )
            loadTasteModel()
            setToast("Signal deleted.")
        }
    }

    fun resetTaste() {
        viewModelScope.launch {
            safeRepositoryCall { repository.resetTaste() }.fold(
                onFailure = { error -> _ui.value = _ui.value.copy(error = error.message) },
                onSuccess = { result ->
                    _state.value = ProductState()
                    _playNext.value = null
                    _picks.value = emptyList()
                    _tasteModel.value = null
                    _dossier.value = DossierUiState.Idle
                    _onboardingCompleted.value = false
                    _ui.value = _ui.value.copy(
                        pendingSync = result.pendingSync,
                        showingStaleData = false,
                    )
                    setToast(if (result.pendingSync) "Taste reset locally; waiting to sync" else "Taste reset")
                },
            )
        }
    }

    fun applyDecisionFeedback(gameId: String, feedback: ProductDecisionFeedback) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            val undo = PendingDecisionUndo(
                gameId = gameId,
                previousState = _state.value.user.gameStates[gameId],
                previousProductState = _state.value,
                previousPlayNext = _playNext.value,
                previousPicks = _picks.value,
                previousExcludedIds = _excludedIds.value,
            )
            val write = try {
                repository.applyFeedback(gameId, feedback)
            } catch (error: Exception) {
                _ui.value = _ui.value.copy(saving = false)
                setToast(error.message ?: "Could not save feedback")
                return@launch
            }
            if (write is RepositoryResult.Failure) {
                _ui.value = _ui.value.copy(saving = false)
                setToast(write.error.message)
                return@launch
            }
            if (write is RepositoryResult.Success) {
                _ui.value = _ui.value.copy(
                    saving = false,
                    pendingSync = _ui.value.pendingSync || write.pendingSync,
                )
            }
            val existing = _state.value.user.gameStates[gameId]
            val next = ProductGameStateTransitions.applyFeedback(existing, gameId, feedback)
            updateGameState(next)
            if (!next.inPlayfitPicks) {
                _picks.value = _picks.value.filterNot { it.game.gameId == gameId }
                updateSavedPickIds(gameId = gameId, picked = false)
            }
            if (feedback == ProductDecisionFeedback.NotForMe) {
                _excludedIds.value = _excludedIds.value + gameId
                removeRecommendation(gameId)
                refreshRecommendationsAfterActionIfNeeded()
            } else if (feedback.isPlayedFeedback()) {
                removeRecommendation(gameId)
                refreshRecommendationsAfterActionIfNeeded()
            }
            rebuildProfileFromCurrentSignals()
            loadTasteModel()
            val message = when (feedback) {
                ProductDecisionFeedback.NotForMe -> "Noted. Playfit will find a better fit."
                ProductDecisionFeedback.PlayedLoved -> "Already played and loved. Playfit will learn from it."
                ProductDecisionFeedback.PlayedLiked -> "Already played and liked."
                ProductDecisionFeedback.PlayedMixed -> "Marked as mixed. Playfit will tune around it."
                ProductDecisionFeedback.PlayedDropped -> "Marked as dropped. Playfit will steer away."
                ProductDecisionFeedback.Play -> "Set as playing. Your next pick will adapt around it."
                ProductDecisionFeedback.Later -> "Saved for later. Playfit will look past it for now."
                ProductDecisionFeedback.Loved -> "Marked as loved."
                ProductDecisionFeedback.Liked -> "Marked as liked."
                ProductDecisionFeedback.Mixed -> "Marked as mixed."
            }
            pendingDecisionUndo = undo
            setToast(message, actionLabel = "Undo")
        }
    }

    fun undoLastDecision() {
        val undo = pendingDecisionUndo ?: return
        pendingDecisionUndo = null
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true, error = null)
            val write = if (undo.previousState == null) {
                safeRepositoryCall { repository.deleteGameState(undo.gameId) }
            } else {
                safeRepositoryCall { repository.saveGameState(undo.gameId, undo.previousState) }
            }
            write.fold(
                onFailure = { error ->
                    _ui.value = _ui.value.copy(saving = false, error = error.message)
                    setToast(error.message)
                },
                onSuccess = { result ->
                    _playNext.value = undo.previousPlayNext
                    _picks.value = undo.previousPicks
                    _excludedIds.value = undo.previousExcludedIds
                    _ui.value = _ui.value.copy(
                        saving = false,
                        pendingSync = _ui.value.pendingSync || result.pendingSync,
                    )
                    rebuildProfileFromCurrentSignals()
                    // Rebuild may complete an earlier feedback request; restore the snapshot
                    // last so an Undo always wins over that stale state update.
                    _state.value = undo.previousProductState
                    loadTasteModel()
                    setToast("Undone.")
                },
            )
        }
    }

    fun saveGameState(gameId: String, gameState: ProductGameState) {
        debouncedSave {
            repository.saveGameState(gameId, gameState)
        }
    }

    private fun debouncedSave(block: suspend () -> RepositoryResult<Unit>) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true)
            delay(1000)
            try {
                block().fold(
                    onFailure = { error ->
                        _ui.value = _ui.value.copy(saving = false, error = error.message)
                    },
                    onSuccess = { result ->
                        _ui.value = _ui.value.copy(
                            saving = false,
                            pendingSync = _ui.value.pendingSync || result.pendingSync,
                        )
                    },
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.message)
            }
        }
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }

    suspend fun getDeviceId(): String = authManager.deviceId

    private fun setToast(message: String, actionLabel: String? = null) {
        _ui.value = _ui.value.copy(toast = message, toastActionLabel = actionLabel)
    }

    fun clearToast() {
        _ui.value = _ui.value.copy(toast = null, toastActionLabel = null)
    }

    private fun clearPlayfitDataAfterProfileDeletion() {
        pendingDecisionUndo = null
        _state.value = ProductState()
        _playNext.value = null
        _picks.value = emptyList()
        _tasteModel.value = null
        _dossier.value = DossierUiState.Idle
        _excludedIds.value = emptySet()
        _onboardingCompleted.value = false
        _ui.value = _ui.value.copy(saving = false, pendingSync = false, showingStaleData = false)
    }

    private fun updateGameState(next: ProductGameState) {
        _state.value = _state.value.copy(
            user = _state.value.user.copy(
                gameStates = _state.value.user.gameStates + (next.gameId to next),
            ),
        )
    }

    private fun updateSavedPickIds(gameId: String, picked: Boolean) {
        _playNext.value = playNextQueueCoordinator.withSavedPick(_playNext.value, gameId, picked)
    }

    private fun removeRecommendation(gameId: String) {
        _playNext.value = playNextQueueCoordinator.withoutRecommendation(_playNext.value, gameId)
    }

    private suspend fun refreshSecondaryData(includeState: Boolean = false) {
        if (includeState) {
            safeRepositoryCall { repository.getState() }.fold(
                onSuccess = { result -> _state.value = result.data },
                onFailure = {},
            )
        }
        safeRepositoryCall { repository.getPicks() }.fold(
            onSuccess = { result -> _picks.value = result.data },
            onFailure = {},
        )
        safeRepositoryCall { repository.getTasteModel() }.fold(
            onSuccess = { result -> _tasteModel.value = result.data },
            onFailure = {},
        )
    }

    private suspend fun refreshRecommendationsAfterActionIfNeeded() {
        if (!playNextQueueCoordinator.shouldRefreshAfterAction(_playNext.value)) return

        _ui.value = _ui.value.copy(refreshing = true)
        val previousStateVersion = _playNext.value?.stateVersion
        safeRepositoryCall { repository.refreshRecommendations() }.fold(
            onFailure = {
                _ui.value = _ui.value.copy(refreshing = false)
            },
            onSuccess = { result ->
                _playNext.value = playNextQueueCoordinator.mergeFreshIfNew(
                    current = _playNext.value,
                    fresh = result.data,
                    excludedIds = _excludedIds.value,
                )
                _ui.value = _ui.value.copy(
                    refreshing = false,
                    showingStaleData = result.isStale,
                )
                refreshSecondaryData(includeState = previousStateVersion != result.data.stateVersion)
            },
        )
    }

    private suspend fun rebuildProfileFromCurrentSignals() {
        val current = _state.value
        safeRepositoryCall {
            repository.rebuildTasteProfile(
                current.user.onboarding,
                current.user.onboardingCompletedAt,
            )
        }.fold(
            onFailure = { error -> _ui.value = _ui.value.copy(error = error.message) },
            onSuccess = { result ->
                if (!result.isStale) {
                    _state.value = _state.value.copy(
                        user = _state.value.user.copy(profile = result.data),
                    )
                }
                _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || result.pendingSync)
            },
        )
    }

    private fun ProductDecisionFeedback.isPlayedFeedback(): Boolean = when (this) {
        ProductDecisionFeedback.PlayedLoved,
        ProductDecisionFeedback.PlayedLiked,
        ProductDecisionFeedback.PlayedMixed,
        ProductDecisionFeedback.PlayedDropped -> true
        else -> false
    }

    private fun ProductPlayStatus?.isTerminal(): Boolean = when (this) {
        ProductPlayStatus.Beaten,
        ProductPlayStatus.Completed,
        ProductPlayStatus.Abandoned -> true
        else -> false
    }

    private fun ProductGameState.isInert(): Boolean =
        status == null && rating == null && !inPlayfitPicks && !inBacklog &&
            !inWishlist && !excluded

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(mode.apiValue)
        }
    }

    private suspend fun <T> safeRepositoryCall(
        block: suspend () -> RepositoryResult<T>,
    ): RepositoryResult<T> = try {
        block()
    } catch (error: Exception) {
        RepositoryResult.Failure(
            RepositoryError.Unknown(error.message ?: "Local data could not be read."),
        )
    }

    private companion object {
        const val MAX_ONBOARDING_RECS_ATTEMPTS = 3
        const val ONBOARDING_RECS_RETRY_DELAY_MS = 1500L
        const val SEARCH_PAGE_SIZE = 24
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
