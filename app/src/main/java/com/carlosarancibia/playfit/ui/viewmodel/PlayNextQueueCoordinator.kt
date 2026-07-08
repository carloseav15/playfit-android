package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.ProductPlayNextModel

internal class PlayNextQueueCoordinator(
    private val minVisibleRecommendationsBeforeRefresh: Int = 1,
) {
    fun withSavedPick(model: ProductPlayNextModel?, gameId: String, picked: Boolean): ProductPlayNextModel? {
        model ?: return null
        val ids = model.savedPickIds.toMutableSet().apply {
            if (picked) add(gameId) else remove(gameId)
        }
        return model.copy(savedPickIds = ids.toList())
    }

    fun withoutRecommendation(model: ProductPlayNextModel?, gameId: String): ProductPlayNextModel? {
        model ?: return null
        val remaining = buildList {
            model.primary?.let(::add)
            addAll(model.alternatives)
        }.filterNot { it.game.gameId == gameId }

        val nextPrimary = if (model.primary?.game?.gameId == gameId) {
            remaining.firstOrNull()
        } else {
            model.primary
        }

        return model.copy(
            primary = nextPrimary,
            alternatives = remaining.filterNot { it.game.gameId == nextPrimary?.game?.gameId },
        )
    }

    fun shouldRefreshAfterAction(model: ProductPlayNextModel?): Boolean =
        (model?.visibleRecommendationCount() ?: 0) <= minVisibleRecommendationsBeforeRefresh

    fun mergeFreshIfNew(
        current: ProductPlayNextModel?,
        fresh: ProductPlayNextModel,
        excludedIds: Set<String>,
    ): ProductPlayNextModel? {
        val currentIds = current.visibleRecommendationIds()
        val freshIds = fresh.visibleRecommendationIds()
        if (freshIds.none { it !in currentIds && it !in excludedIds }) return current
        return fresh.copy(
            primary = fresh.primary?.takeUnless { it.game.gameId in excludedIds },
            alternatives = fresh.alternatives.filterNot { it.game.gameId in excludedIds },
        )
    }

    private fun ProductPlayNextModel?.visibleRecommendationIds(): Set<String> = buildSet {
        this@visibleRecommendationIds?.primary?.let { add(it.game.gameId) }
        this@visibleRecommendationIds?.alternatives.orEmpty().forEach { add(it.game.gameId) }
    }

    private fun ProductPlayNextModel.visibleRecommendationCount(): Int =
        listOfNotNull(primary).size + alternatives.size
}
