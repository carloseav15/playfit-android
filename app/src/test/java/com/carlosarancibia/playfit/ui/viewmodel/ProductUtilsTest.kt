package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.model.ProductConfidence
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductUtilsTest {

    private fun makeEntry(
        affinityScore: Double = 70.0,
        riskScore: Double = 30.0,
        confidence: ProductConfidence = ProductConfidence.High,
    ) = RankedSeedGame(
        game = SeedGame(gameId = "test", title = "Test Game"),
        affinityScore = affinityScore,
        riskScore = riskScore,
        confidence = confidence,
        fitReasons = listOf("Great fit"),
        cautionReasons = listOf("Watch out"),
        platformAvailability = com.carlosarancibia.playfit.model.PlatformAvailability.Available,
        accessStatus = com.carlosarancibia.playfit.model.GameAccessStatus.Playable,
    )

    @Test
    fun `matchQualityLabel returns Strong match for high scores`() {
        assertEquals("Strong match", ProductUtils.matchQualityLabel(80.0))
        assertEquals("Strong match", ProductUtils.matchQualityLabel(100.0))
    }

    @Test
    fun `matchQualityLabel returns Promising for medium scores`() {
        assertEquals("Promising", ProductUtils.matchQualityLabel(65.0))
    }

    @Test
    fun `matchQualityLabel returns Moderate for low-medium scores`() {
        assertEquals("Moderate match", ProductUtils.matchQualityLabel(40.0))
    }

    @Test
    fun `matchQualityLabel returns Early match for low scores`() {
        assertEquals("Early match", ProductUtils.matchQualityLabel(20.0))
    }

    @Test
    fun `watchOutLabel returns High friction for high risk`() {
        assertEquals("High friction", ProductUtils.watchOutLabel(60.0))
    }

    @Test
    fun `watchOutLabel returns Some watch-outs for medium risk`() {
        assertEquals("Some watch-outs", ProductUtils.watchOutLabel(40.0))
    }

    @Test
    fun `watchOutLabel returns Low watch-out for low risk`() {
        assertEquals("Low watch-out", ProductUtils.watchOutLabel(20.0))
    }

    @Test
    fun `watchOutLabel returns Clear read for very low risk`() {
        assertEquals("Clear read", ProductUtils.watchOutLabel(10.0))
    }

    @Test
    fun `confidenceLabel returns correct labels`() {
        assertEquals("Strong signal", ProductUtils.confidenceLabel(ProductConfidence.High))
        assertEquals("Building signal", ProductUtils.confidenceLabel(ProductConfidence.Medium))
        assertEquals("First look", ProductUtils.confidenceLabel(ProductConfidence.Low))
    }

    @Test
    fun `decisionLabel returns Watch out for high risk`() {
        val entry = makeEntry(riskScore = 60.0)
        assertEquals("Watch out", ProductUtils.decisionLabel(entry))
    }

    @Test
    fun `decisionLabel returns Too early for low confidence`() {
        val entry = makeEntry(confidence = ProductConfidence.Low)
        assertEquals("Too early to tell", ProductUtils.decisionLabel(entry))
    }

    @Test
    fun `decisionLabel returns Strong match for high affinity`() {
        val entry = makeEntry(affinityScore = 80.0, riskScore = 30.0)
        assertEquals("Strong match", ProductUtils.decisionLabel(entry))
    }

    @Test
    fun `decisionLabel returns Promising for medium affinity`() {
        val entry = makeEntry(affinityScore = 65.0, riskScore = 40.0)
        assertEquals("Promising", ProductUtils.decisionLabel(entry))
    }
}
