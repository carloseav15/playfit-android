package com.carlosarancibia.playfit.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductOnboardingRulesTest {
    @Test
    fun `valid calibration requires platform exactly three loved and one miss`() {
        assertTrue(ProductOnboardingRules.canComplete(validDraft()))
    }

    @Test
    fun `calibration rejects missing platforms`() {
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(platforms = emptyList())))
    }

    @Test
    fun `calibration rejects fewer or more than three loved games`() {
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(likedGameIds = listOf("a", "b"))))
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(likedGameIds = listOf("a", "b", "c", "d"))))
    }

    @Test
    fun `calibration rejects anything other than one missed game`() {
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(dislikedGameIds = emptyList())))
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(dislikedGameIds = listOf("x", "y"))))
    }

    @Test
    fun `calibration rejects duplicate and overlapping games`() {
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(likedGameIds = listOf("a", "a", "c"))))
        assertFalse(ProductOnboardingRules.canComplete(validDraft().copy(dislikedGameIds = listOf("a"))))
    }

    private fun validDraft() = ProductOnboardingDraft(
        platforms = listOf(ProductPlatformSelection("ps5", ProductAccessStatus.Available)),
        likedGameIds = listOf("a", "b", "c"),
        dislikedGameIds = listOf("miss"),
    )
}
