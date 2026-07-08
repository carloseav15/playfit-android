package com.carlosarancibia.playfit.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductOnboardingRulesTest {
    @Test
    fun `valid calibration requires platform exactly three loved and one miss`() {
        assertTrue(ProductOnboardingRules.validate(validDraft()) is ProductOnboardingValidation.Valid)
    }

    @Test
    fun `calibration rejects missing platforms`() {
        assertInvalid(validDraft().copy(platforms = emptyList()))
    }

    @Test
    fun `calibration rejects fewer or more than three loved games`() {
        assertInvalid(validDraft().copy(likedGameIds = listOf("a", "b")))
        assertInvalid(validDraft().copy(likedGameIds = listOf("a", "b", "c", "d")))
    }

    @Test
    fun `calibration rejects anything other than one missed game`() {
        assertInvalid(validDraft().copy(dislikedGameIds = emptyList()))
        assertInvalid(validDraft().copy(dislikedGameIds = listOf("x", "y")))
    }

    @Test
    fun `calibration rejects duplicate and overlapping games`() {
        assertInvalid(validDraft().copy(likedGameIds = listOf("a", "a", "c")))
        assertInvalid(validDraft().copy(dislikedGameIds = listOf("a")))
    }

    private fun assertInvalid(draft: ProductOnboardingDraft) {
        assertFalse(ProductOnboardingRules.validate(draft) is ProductOnboardingValidation.Valid)
    }

    private fun validDraft() = ProductOnboardingDraft(
        platforms = listOf(ProductPlatformSelection("ps5", ProductAccessStatus.Available)),
        likedGameIds = listOf("a", "b", "c"),
        dislikedGameIds = listOf("miss"),
    )
}
