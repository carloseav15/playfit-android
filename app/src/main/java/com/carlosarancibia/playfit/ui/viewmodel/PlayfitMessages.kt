package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.ProductDecisionFeedback

/** Centralized user-facing copy used by state transitions and undo feedback. */
internal object PlayfitMessages {
    const val skipped = "Skipped"
    const val removedFromPicks = "Removed from picks."
    const val couldNotRemovePick = "Could not remove pick."
    const val signalDeleted = "Signal deleted."
    const val undone = "Undone."

    fun decisionFeedback(feedback: ProductDecisionFeedback): String = when (feedback) {
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
}
