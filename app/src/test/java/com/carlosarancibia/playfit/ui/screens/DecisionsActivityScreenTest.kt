package com.carlosarancibia.playfit.ui.screens

import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DecisionsActivityScreenTest {

    @Test
    fun `change signal options preserve played semantics`() {
        val options = playedChangeSignalOptions.toMap()

        assertEquals(ProductDecisionFeedback.PlayedLoved, options.getValue("Loved"))
        assertEquals(ProductDecisionFeedback.PlayedLiked, options.getValue("Liked"))
        assertEquals(ProductDecisionFeedback.PlayedMixed, options.getValue("Mixed"))
        assertEquals(ProductDecisionFeedback.PlayedDropped, options.getValue("Dropped"))
        assertEquals(ProductDecisionFeedback.NotForMe, options.getValue("Not For Me"))
        assertFalse(options.values.contains(ProductDecisionFeedback.Loved))
        assertFalse(options.values.contains(ProductDecisionFeedback.Liked))
        assertFalse(options.values.contains(ProductDecisionFeedback.Mixed))
    }
}
