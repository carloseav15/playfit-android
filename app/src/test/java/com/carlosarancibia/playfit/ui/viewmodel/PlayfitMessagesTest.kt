package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayfitMessagesTest {
    @Test
    fun everyDecisionFeedbackHasStableUserCopy() {
        assertEquals(
            "Noted. Playfit will find a better fit.",
            PlayfitMessages.decisionFeedback(ProductDecisionFeedback.NotForMe),
        )
        assertEquals(
            "Saved for later. Playfit will look past it for now.",
            PlayfitMessages.decisionFeedback(ProductDecisionFeedback.Later),
        )
        assertEquals(
            "Marked as dropped. Playfit will steer away.",
            PlayfitMessages.decisionFeedback(ProductDecisionFeedback.PlayedDropped),
        )
    }
}
