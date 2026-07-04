package com.carlosarancibia.playfit.model

import java.time.Instant

/**
 * Android equivalent of the state-transition rules in PlayfitLogic on iOS.
 * These functions are intentionally pure so UI and persistence cannot assign
 * different meanings to the same user decision.
 */
object ProductGameStateTransitions {

    fun applyFeedback(
        existing: ProductGameState?,
        gameId: String,
        feedback: ProductDecisionFeedback,
        timestamp: String = Instant.now().toString(),
    ): ProductGameState {
        var next = existing ?: ProductGameState(
            gameId = gameId,
            title = "",
            createdAt = timestamp,
        )

        if (next.createdAt.isBlank()) {
            next = next.copy(createdAt = timestamp)
        }
        next = next.copy(updatedAt = timestamp)

        next = when (feedback) {
            ProductDecisionFeedback.Play -> next.copy(
                status = ProductPlayStatus.Playing,
                inBacklog = false,
                inPlayfitPicks = false,
                excluded = false,
            )
            ProductDecisionFeedback.Later -> next.copy(
                status = ProductPlayStatus.Shelved,
                inBacklog = true,
                excluded = false,
            )
            ProductDecisionFeedback.PlayedLoved,
            ProductDecisionFeedback.PlayedLiked,
            ProductDecisionFeedback.PlayedMixed -> next.copy(
                status = ProductPlayStatus.Completed,
                inBacklog = false,
                inPlayfitPicks = false,
                excluded = false,
            )
            ProductDecisionFeedback.PlayedDropped -> next.copy(
                status = ProductPlayStatus.Abandoned,
                inBacklog = false,
                inPlayfitPicks = false,
                excluded = true,
            )
            else -> next
        }

        val rating = ratingFor(feedback)
        if (rating != null) {
            val excluded = feedback == ProductDecisionFeedback.NotForMe ||
                feedback == ProductDecisionFeedback.PlayedDropped
            next = next.copy(
                rating = rating,
                excluded = excluded,
                inPlayfitPicks = if (excluded || feedback.isPlayed()) false else next.inPlayfitPicks,
            )
        }

        return next
    }

    fun setPick(
        existing: ProductGameState?,
        gameId: String,
        picked: Boolean,
        timestamp: String = Instant.now().toString(),
    ): ProductGameState {
        val current = existing ?: ProductGameState(
            gameId = gameId,
            title = "",
            createdAt = timestamp,
        )
        return current.copy(
            createdAt = current.createdAt.ifBlank { timestamp },
            updatedAt = timestamp,
            inPlayfitPicks = picked,
        )
    }

    private fun ratingFor(feedback: ProductDecisionFeedback): Double? = when (feedback) {
        ProductDecisionFeedback.Loved,
        ProductDecisionFeedback.PlayedLoved -> 5.0
        ProductDecisionFeedback.Liked,
        ProductDecisionFeedback.PlayedLiked -> 4.0
        ProductDecisionFeedback.Mixed,
        ProductDecisionFeedback.PlayedMixed -> 3.0
        ProductDecisionFeedback.NotForMe,
        ProductDecisionFeedback.PlayedDropped -> 2.0
        ProductDecisionFeedback.Play,
        ProductDecisionFeedback.Later -> null
    }

    private fun ProductDecisionFeedback.isPlayed(): Boolean = when (this) {
        ProductDecisionFeedback.PlayedLoved,
        ProductDecisionFeedback.PlayedLiked,
        ProductDecisionFeedback.PlayedMixed,
        ProductDecisionFeedback.PlayedDropped -> true
        else -> false
    }
}
