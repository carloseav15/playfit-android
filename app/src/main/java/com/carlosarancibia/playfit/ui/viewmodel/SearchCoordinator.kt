package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.SearchGamesPage
import com.carlosarancibia.playfit.data.fold
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.SeedGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SearchCoordinator(
    private val repository: PlayfitRepository,
    private val scope: CoroutineScope,
    private val platforms: () -> List<Platform>,
    private val pageSize: Int = 24,
    private val debounceMs: Long = 250L,
) {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var requestJob: Job? = null
    private var requestSequence = 0L

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        trigger(resetPage = true)
    }

    fun updateFamily(family: String?) {
        val next = if (_state.value.selectedFamily == family) null else family
        _state.value = _state.value.copy(selectedFamily = next)
        trigger(resetPage = true)
    }

    fun loadMore() {
        if (_state.value.loadingMore || !_state.value.hasMore) return
        trigger(resetPage = false)
    }

    fun retry() = trigger(resetPage = true)

    fun reset() {
        requestJob?.cancel()
        requestSequence++
        _state.value = SearchUiState()
    }

    private fun trigger(resetPage: Boolean) {
        requestJob?.cancel()
        val snapshot = _state.value
        val nextPage = if (resetPage) 1 else snapshot.page + 1
        val sequence = ++requestSequence

        requestJob = scope.launch {
            _state.value = _state.value.copy(
                page = nextPage,
                loading = resetPage,
                loadingMore = !resetPage,
                error = null,
            )
            delay(debounceMs)
            if (sequence != requestSequence) return@launch

            val platformIds = snapshot.selectedFamily
                ?.let { family -> platforms().filter { it.family == family }.map { it.platformId } }
                .orEmpty()
            val result = try {
                repository.searchGames(
                    query = snapshot.query,
                    platformIds = platformIds,
                    page = nextPage,
                    pageSize = pageSize,
                )
            } catch (error: Exception) {
                RepositoryResult.Failure(RepositoryError.Unknown(error.message ?: "Search failed."))
            }
            if (sequence != requestSequence) return@launch

            result.fold(
                onSuccess = { success -> applySuccess(success.data, resetPage) },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        loadingMore = false,
                        error = error.message,
                    )
                },
            )
        }
    }

    private fun applySuccess(page: SearchGamesPage, resetPage: Boolean) {
        _state.value = _state.value.copy(
            results = if (resetPage) page.games else mergeSearchResults(_state.value.results, page.games),
            total = page.total,
            loading = false,
            loadingMore = false,
            error = null,
        )
    }
}

internal fun mergeSearchResults(existing: List<SeedGame>, incoming: List<SeedGame>): List<SeedGame> {
    val seen = existing.mapTo(mutableSetOf()) { it.gameId }
    return existing + incoming.filterNot { it.gameId in seen }
}
