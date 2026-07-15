package com.carlosarancibia.playfit.ui.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.SearchGamesPage
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.data.auth.AuthSessionInfo
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductAccessStatus
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductPlatformSelection
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductPlayStatus
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayfitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: PlayfitRepository
    private lateinit var authManager: AuthManager
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var viewModel: PlayfitViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)
        authManager = mockk(relaxed = true)
        preferencesDataStore = mockk(relaxed = true)

        every { preferencesDataStore.onboardingCompleted } returns MutableStateFlow(false)
        every { repository.observePendingSync() } returns MutableStateFlow(false)

        val restoredSession = AuthSessionInfo(
            userId = "user-1",
            email = null,
            isAnonymous = true,
        )
        every { authManager.session } returns MutableStateFlow(restoredSession)
        every { authManager.pendingPasswordRecovery } returns MutableStateFlow(null)
        coEvery { authManager.restoreSession() } returns restoredSession
        coEvery { authManager.signInAnonymously() } returns AuthResult.Success(restoredSession)
        coEvery { authManager.signOut() } returns AuthResult.Success()
        coEvery { repository.getState() } returns success(ProductState())
        coEvery { repository.getTodayRecommendations() } returns success(ProductPlayNextModel(
            primary = recommendation("game1"),
            alternatives = emptyList(),
            savedPickIds = emptyList(),
            stateVersion = "v1",
        ))
        coEvery { repository.getPicks() } returns success(emptyList())
        coEvery { repository.getTasteModel() } returns success(com.carlosarancibia.playfit.model.ProductTasteModel())
        coEvery { repository.refreshRecommendations() } returns success(ProductPlayNextModel(
            primary = null,
            alternatives = emptyList(),
            savedPickIds = emptyList(),
            stateVersion = "v1",
        ))
        coEvery { repository.togglePick(any(), any()) } returns success(Unit, pendingSync = true)
        coEvery { repository.applyFeedback(any(), any()) } returns success(Unit, pendingSync = true)
        coEvery { repository.saveOnboarding(any(), any()) } returns success(Unit)
        coEvery { repository.rebuildTasteProfile(any(), any()) } returns success(ProductProfile())
        coEvery { repository.deleteGameState(any()) } returns success(Unit, pendingSync = true)
        coEvery { repository.resetTaste() } returns success(Unit)
        coEvery { repository.deleteCloudProfile() } returns success(Unit)

        viewModel = PlayfitViewModel(repository, authManager, preferencesDataStore)
    }

    @Test
    fun `auth state exposes account details and available actions`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val auth = viewModel.authState.value

        assertTrue(auth.isAuthenticated)
        assertTrue(auth.isAnonymous)
        assertEquals("user-1", auth.userId)
        assertTrue(auth.canLinkGoogle)
        assertTrue(auth.canSignOut)
        assertTrue(auth.canDeleteAccount)
    }

    @Test
    fun `delete account deletes the cloud profile before signing out`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.deleteAccount()
        advanceUntilIdle()

        coVerify { repository.deleteCloudProfile() }
        coVerify { authManager.signOut() }
        assertEquals("Cloud profile deleted.", viewModel.ui.value.toast)
        assertTrue(viewModel.state.value.user.gameStates.isEmpty())
    }

    @Test
    fun `pendingPasswordRecovery mirrors AuthManager state`() = runTest(testDispatcher) {
        val recoverySession = mockk<UserSession>(relaxed = true)
        every { authManager.pendingPasswordRecovery } returns MutableStateFlow(recoverySession)

        val model = newViewModel()
        advanceUntilIdle()

        assertEquals(recoverySession, model.pendingPasswordRecovery.value)
    }

    @Test
    fun `updatePassword delegates to AuthManager`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { authManager.updatePassword("newPass123") } returns AuthResult.Success(
            AuthSessionInfo(userId = "user-1", email = "user@example.com", isAnonymous = false),
        )

        val result = viewModel.updatePassword("newPass123")

        coVerify { authManager.updatePassword("newPass123") }
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `cancelPendingPasswordRecovery delegates to AuthManager`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.cancelPendingPasswordRecovery()

        verify { authManager.cancelPendingPasswordRecovery() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init restores session before loading initial data`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coVerify { authManager.restoreSession() }
        coVerify(exactly = 0) { authManager.signInAnonymously() }
        coVerify { repository.getState() }
        coVerify { repository.getTodayRecommendations() }
        coVerify { repository.getPicks() }
    }

    @Test
    fun `init creates anonymous session only when storage has no session`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val guest = AuthSessionInfo(userId = "guest-1", email = null, isAnonymous = true)
        every { authManager.session } returns MutableStateFlow(null)
        coEvery { authManager.restoreSession() } returns null
        coEvery { authManager.signInAnonymously() } returns AuthResult.Success(guest)

        PlayfitViewModel(repository, authManager, preferencesDataStore)
        advanceUntilIdle()

        coVerify { authManager.signInAnonymously() }
    }

    @Test
    fun `togglePick adds pick when not present`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.getPicks() } returns success(listOf(
            RankedSeedGame(
                game = SeedGame(gameId = "game1", title = "Game 1"),
                affinityScore = 80.0,
                riskScore = 20.0,
                confidence = com.carlosarancibia.playfit.model.ProductConfidence.High,
                fitReasons = emptyList(),
                cautionReasons = emptyList(),
                platformAvailability = com.carlosarancibia.playfit.model.PlatformAvailability.Available,
                accessStatus = com.carlosarancibia.playfit.model.GameAccessStatus.Playable,
            ),
        ))

        viewModel.togglePick("game1")
        advanceUntilIdle()

        coVerify { repository.togglePick("game1", true) }
        val picks = viewModel.picks.value
        assertEquals(1, picks.size)
        assertEquals("game1", picks.first().game.gameId)
    }

    @Test
    fun `togglePick refreshes recommendations after removing the last visible candidate`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val refreshed = ProductPlayNextModel(
            primary = recommendation("game2"),
            alternatives = emptyList(),
            savedPickIds = listOf("game1"),
            stateVersion = "v2",
        )
        coEvery { repository.refreshRecommendations() } returns success(refreshed)

        viewModel.togglePick("game1")
        advanceUntilIdle()

        coVerify { repository.refreshRecommendations() }
        assertEquals("game2", viewModel.playNext.value?.primary?.game?.gameId)
    }

    @Test
    fun `togglePick removes pick when already present`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.getPicks() } returnsMany listOf(
            success(listOf(
                RankedSeedGame(
                    game = SeedGame(gameId = "game1", title = "Game 1"),
                    affinityScore = 80.0, riskScore = 20.0,
                    confidence = com.carlosarancibia.playfit.model.ProductConfidence.High,
                    fitReasons = emptyList(), cautionReasons = emptyList(),
                    platformAvailability = com.carlosarancibia.playfit.model.PlatformAvailability.Available,
                    accessStatus = com.carlosarancibia.playfit.model.GameAccessStatus.Playable,
                ),
            )),
            success(emptyList()),
        )

        viewModel.togglePick("game1")
        advanceUntilIdle()

        viewModel.togglePick("game1")
        advanceUntilIdle()

        coVerify { repository.togglePick("game1", false) }
        assertTrue(viewModel.picks.value.isEmpty())
    }

    @Test
    fun `togglePick rejects terminal game without repository write`() = runTest(testDispatcher) {
        val initialState = ProductState().let { state ->
            state.copy(
                user = state.user.copy(
                    gameStates = mapOf(
                        "game1" to ProductGameState(
                            gameId = "game1",
                            title = "Game 1",
                            status = ProductPlayStatus.Completed,
                        ),
                    ),
                ),
            )
        }
        coEvery { repository.getState() } returns success(initialState)
        val model = newViewModel()
        advanceUntilIdle()

        model.togglePick("game1")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.togglePick("game1", true) }
        assertTrue(model.picks.value.isEmpty())
        assertNotNull(model.ui.value.toast)
    }

    @Test
    fun `applyDecisionFeedback calls repository and shows toast`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.applyFeedback(any(), any()) } returns success(Unit, pendingSync = true)
        coEvery { repository.getPicks() } returns success(emptyList())

        viewModel.applyDecisionFeedback("game1", ProductDecisionFeedback.Loved)
        advanceUntilIdle()

        coVerify { repository.applyFeedback("game1", ProductDecisionFeedback.Loved) }
        assertNotNull(viewModel.ui.value.toast)
    }

    @Test
    fun `not for me uses the shared cross-platform toast copy`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.applyDecisionFeedback("game1", ProductDecisionFeedback.NotForMe)
        advanceUntilIdle()

        assertEquals("Noted. Playfit will find a better fit.", viewModel.ui.value.toast)
        assertEquals("Undo", viewModel.ui.value.toastActionLabel)
    }

    @Test
    fun `undo not for me removes a newly created game state and restores recommendations`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val before = viewModel.playNext.value

        viewModel.applyDecisionFeedback("game1", ProductDecisionFeedback.NotForMe)
        advanceUntilIdle()
        viewModel.undoLastDecision()
        advanceUntilIdle()

        coVerify { repository.deleteGameState("game1") }
        assertTrue(viewModel.state.value.user.gameStates.isEmpty())
        assertEquals(before, viewModel.playNext.value)
        assertEquals("Undone.", viewModel.ui.value.toast)
    }

    @Test
    fun `undo restores the previous decision state`() = runTest(testDispatcher) {
        val previous = ProductGameState(
            gameId = "game1",
            title = "Game 1",
            status = ProductPlayStatus.OnHold,
            inPlayfitPicks = true,
        )
        val initialState = ProductState().let { state ->
            state.copy(user = state.user.copy(gameStates = mapOf("game1" to previous)))
        }
        coEvery { repository.getState() } returns success(initialState)
        val model = newViewModel()
        advanceUntilIdle()

        model.applyDecisionFeedback("game1", ProductDecisionFeedback.NotForMe)
        advanceUntilIdle()
        model.undoLastDecision()
        advanceUntilIdle()

        coVerify { repository.saveGameState("game1", previous) }
    }

    @Test
    fun `show another removes recommendation without writing negative feedback`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.skipRecommendation("game1")

        assertTrue(viewModel.playNext.value?.primary == null)
        coVerify(exactly = 0) { repository.applyFeedback(any(), any()) }
        assertTrue(viewModel.state.value.user.gameStates.isEmpty())
    }

    @Test
    fun `played feedback updates state and removes recommendation`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.applyFeedback(any(), any()) } returns success(Unit, pendingSync = true)

        viewModel.applyDecisionFeedback("game1", ProductDecisionFeedback.PlayedLoved)
        advanceUntilIdle()

        val state = viewModel.state.value.user.gameStates.getValue("game1")
        assertEquals(com.carlosarancibia.playfit.model.ProductPlayStatus.Completed, state.status)
        assertEquals(5.0, state.rating)
        assertFalse(state.inPlayfitPicks)
        assertTrue(viewModel.playNext.value?.primary == null)
    }

    @Test
    fun `searchGames returns results from repository`() = runTest(testDispatcher) {
        val expected = listOf(
            SeedGame(gameId = "g1", title = "Game 1"),
            SeedGame(gameId = "g2", title = "Game 2"),
        )
        coEvery { repository.searchGames("game", 20) } returns success(SearchGamesPage(expected, expected.size))

        val result = viewModel.searchGames("game")
        assertEquals(expected, result)
    }

    @Test
    fun `searchGames surfaces error to caller`() = runTest(testDispatcher) {
        coEvery { repository.searchGames(any(), any()) } returns RepositoryResult.Failure(
            RepositoryError.Network("API error"),
        )

        try {
            viewModel.searchGames("game")
            org.junit.Assert.fail("Expected searchGames to throw")
        } catch (error: IllegalStateException) {
            assertEquals("API error", error.message)
        }
        assertEquals("API error", viewModel.ui.value.error)
    }

    @Test
    fun `catalog search debounces rapid typing into a single request for the latest query`() = runTest(testDispatcher) {
        coEvery { repository.searchGames(query = any(), platformIds = any(), page = any(), pageSize = any()) } returns
            success(SearchGamesPage(listOf(SeedGame(gameId = "mario", title = "Mario")), 1))

        viewModel.updateSearchQuery("m")
        viewModel.updateSearchQuery("ma")
        viewModel.updateSearchQuery("mar")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.searchGames(query = "m", platformIds = any(), page = any(), pageSize = any()) }
        coVerify(exactly = 0) { repository.searchGames(query = "ma", platformIds = any(), page = any(), pageSize = any()) }
        coVerify(exactly = 1) { repository.searchGames(query = "mar", platformIds = any(), page = any(), pageSize = any()) }
        assertEquals("mar", viewModel.search.value.query)
        assertEquals(listOf("mario"), viewModel.search.value.results.map { it.gameId })
    }

    @Test
    fun `catalog search family filter toggles off when reselected`() = runTest(testDispatcher) {
        coEvery { repository.searchGames(query = any(), platformIds = any(), page = any(), pageSize = any()) } returns
            success(SearchGamesPage(emptyList(), 0))

        val nintendoPlatformIds = com.carlosarancibia.playfit.model.fallbackPlatforms
            .filter { it.family == "nintendo" }
            .map { it.platformId }

        viewModel.updateSearchFamily("nintendo")
        advanceUntilIdle()
        assertEquals("nintendo", viewModel.search.value.selectedFamily)
        coVerify {
            repository.searchGames(query = "", platformIds = nintendoPlatformIds, page = 1, pageSize = 24)
        }

        viewModel.updateSearchFamily("nintendo")
        advanceUntilIdle()
        assertEquals(null, viewModel.search.value.selectedFamily)
        coVerify { repository.searchGames(query = "", platformIds = emptyList(), page = 1, pageSize = 24) }
    }

    @Test
    fun `catalog search loadMore appends results and stops when there is no more`() = runTest(testDispatcher) {
        coEvery { repository.searchGames(query = "zelda", platformIds = any(), page = 1, pageSize = 24) } returns
            success(SearchGamesPage(listOf(SeedGame(gameId = "z1", title = "Zelda 1")), 2))
        coEvery { repository.searchGames(query = "zelda", platformIds = any(), page = 2, pageSize = 24) } returns
            success(SearchGamesPage(listOf(SeedGame(gameId = "z2", title = "Zelda 2")), 2))

        viewModel.updateSearchQuery("zelda")
        advanceUntilIdle()
        assertTrue(viewModel.search.value.hasMore)

        viewModel.loadMoreSearchResults()
        advanceUntilIdle()

        assertEquals(listOf("z1", "z2"), viewModel.search.value.results.map { it.gameId })
        assertFalse(viewModel.search.value.hasMore)

        viewModel.loadMoreSearchResults()
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.searchGames(query = "zelda", platformIds = any(), page = 3, pageSize = 24) }
    }

    @Test
    fun `removePick calls togglePick with false`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.togglePick(any(), any()) } returns success(Unit, pendingSync = true)
        coEvery { repository.getPicks() } returns success(emptyList())

        viewModel.removePick("game1")
        advanceUntilIdle()

        coVerify { repository.togglePick("game1", false) }
    }

    @Test
    fun `refreshRecommendations updates playNext and picks`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val updatedModel = ProductPlayNextModel(
            primary = null,
            alternatives = emptyList(),
            savedPickIds = emptyList(),
            stateVersion = "v2",
        )
        coEvery { repository.refreshRecommendations() } returns success(updatedModel)
        coEvery { repository.getPicks() } returns success(emptyList())

        viewModel.refreshRecommendations()
        advanceUntilIdle()

        assertEquals("v2", viewModel.playNext.value?.stateVersion)
        coVerify { repository.refreshRecommendations() }
    }

    @Test
    fun `refresh failure preserves current recommendations and exposes error`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.refreshRecommendations() } returns RepositoryResult.Failure(
            RepositoryError.Server(statusCode = 503, message = "Service unavailable"),
        )

        viewModel.refreshRecommendations()
        advanceUntilIdle()

        assertEquals("game1", viewModel.playNext.value?.primary?.game?.gameId)
        assertEquals("Service unavailable", viewModel.ui.value.error)
    }

    @Test
    fun `stale refresh is labeled instead of reported as fresh`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val cached = ProductPlayNextModel(
            primary = recommendation("cached"),
            alternatives = emptyList(),
            savedPickIds = emptyList(),
            stateVersion = "cached-v1",
        )
        coEvery { repository.refreshRecommendations() } returns RepositoryResult.Success(
            data = cached,
            source = DataSource.Cache,
            isStale = true,
        )

        viewModel.refreshRecommendations()
        advanceUntilIdle()

        assertTrue(viewModel.ui.value.showingStaleData)
        assertEquals("Showing saved recommendations", viewModel.ui.value.toast)
    }

    @Test
    fun `invalid onboarding cannot mark calibration complete`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.completeOnboarding(ProductOnboardingDraft())
        advanceUntilIdle()

        assertFalse(viewModel.onboardingCompleted.value)
        assertNotNull(viewModel.ui.value.error)
        coVerify(exactly = 0) { repository.saveOnboarding(any(), any()) }
    }

    @Test
    fun `valid onboarding persists contract before marking complete`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val draft = validOnboardingDraft()

        viewModel.completeOnboarding(draft)
        advanceUntilIdle()

        coVerify { repository.saveOnboarding(draft, any()) }
        coVerify { preferencesDataStore.setSelectedPlatformIds(setOf("ps5")) }
        coVerify { preferencesDataStore.setOnboardingCompleted(true) }
        assertTrue(viewModel.onboardingCompleted.value)
    }

    @Test
    fun `platform changes update product state and persist full onboarding`() = runTest(testDispatcher) {
        val draft = validOnboardingDraft()
        val initialState = ProductState().let { current ->
            current.copy(
                user = current.user.copy(
                    onboarding = draft,
                    onboardingCompletedAt = "2026-07-02T00:00:00Z",
                ),
            )
        }
        coEvery { repository.getState() } returns success(initialState)
        val model = newViewModel()
        advanceUntilIdle()

        model.updatePlatforms(setOf("ps5", "pc"))
        advanceUntilIdle()

        assertEquals(
            setOf("ps5", "pc"),
            model.state.value.user.onboarding.platforms.map { it.platformId }.toSet(),
        )
        coVerify { preferencesDataStore.setSelectedPlatformIds(setOf("ps5", "pc")) }
        coVerify {
            repository.saveOnboarding(
                match { it.platforms.map { platform -> platform.platformId }.toSet() == setOf("ps5", "pc") },
                "2026-07-02T00:00:00Z",
            )
        }
    }

    @Test
    fun `dossier fetches game by id when it is not already loaded`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val fetched = recommendation("remote-game")
        coEvery { repository.getGameRecommendation("remote-game") } returns success(fetched)

        viewModel.loadGameRecommendation("remote-game")
        advanceUntilIdle()

        assertTrue(viewModel.dossier.value is DossierUiState.Success)
        assertEquals(
            "remote-game",
            (viewModel.dossier.value as DossierUiState.Success).entry.game.gameId,
        )
    }

    @Test
    fun `dossier exposes typed error without reporting not found`() = runTest(testDispatcher) {
        advanceUntilIdle()
        coEvery { repository.getGameRecommendation("remote-game") } returns RepositoryResult.Failure(
            RepositoryError.Network("Offline"),
        )

        viewModel.loadGameRecommendation("remote-game")
        advanceUntilIdle()

        assertTrue(viewModel.dossier.value is DossierUiState.Error)
        assertEquals("Offline", (viewModel.dossier.value as DossierUiState.Error).message)
    }

    @Test
    fun `deleting onboarding signal updates draft and persists deletion`() = runTest(testDispatcher) {
        val draft = validOnboardingDraft()
        val initialState = ProductState().let { current ->
            current.copy(
                user = current.user.copy(
                    onboarding = draft,
                    onboardingCompletedAt = "2026-07-02T00:00:00Z",
                    gameStates = mapOf("a" to ProductGameState("a", "A")),
                ),
            )
        }
        coEvery { repository.getState() } returns success(initialState)
        val model = newViewModel()
        advanceUntilIdle()

        model.deleteSignal("a", "onboarding_liked")
        advanceUntilIdle()

        assertFalse("a" in model.state.value.user.onboarding.likedGameIds)
        assertFalse("a" in model.state.value.user.gameStates)
        coVerify { repository.deleteGameState("a") }
        coVerify {
            repository.rebuildTasteProfile(
                match { "a" !in it.likedGameIds },
                "2026-07-02T00:00:00Z",
            )
        }
    }

    @Test
    fun `reset taste clears product data without signing out`() = runTest(testDispatcher) {
        val initialState = ProductState().let { current ->
            current.copy(user = current.user.copy(profile = ProductProfile(summary = "Known")))
        }
        coEvery { repository.getState() } returns success(initialState)
        val model = newViewModel()
        advanceUntilIdle()

        model.resetTaste()
        advanceUntilIdle()

        coVerify { repository.resetTaste() }
        coVerify(exactly = 0) { authManager.signOut() }
        assertTrue(model.state.value.user.gameStates.isEmpty())
        assertTrue(model.state.value.user.profile == null)
        assertFalse(model.onboardingCompleted.value)
    }

    private fun recommendation(gameId: String) = RankedSeedGame(
        game = SeedGame(gameId = gameId, title = "Game $gameId"),
        affinityScore = 80.0,
        riskScore = 20.0,
        confidence = com.carlosarancibia.playfit.model.ProductConfidence.High,
        fitReasons = emptyList(),
        cautionReasons = emptyList(),
        platformAvailability = com.carlosarancibia.playfit.model.PlatformAvailability.Available,
        accessStatus = com.carlosarancibia.playfit.model.GameAccessStatus.Playable,
    )

    private fun validOnboardingDraft() = ProductOnboardingDraft(
        platforms = listOf(ProductPlatformSelection("ps5", ProductAccessStatus.Available)),
        likedGameIds = listOf("a", "b", "c"),
        dislikedGameIds = listOf("miss"),
    )

    private fun newViewModel(): PlayfitViewModel =
        PlayfitViewModel(repository, authManager, preferencesDataStore)

    private fun <T> success(
        value: T,
        pendingSync: Boolean = false,
    ): RepositoryResult.Success<T> = RepositoryResult.Success(
        data = value,
        source = DataSource.Network,
        pendingSync = pendingSync,
    )
}
