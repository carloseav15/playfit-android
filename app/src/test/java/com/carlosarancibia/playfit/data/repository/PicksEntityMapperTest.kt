package com.carlosarancibia.playfit.data.repository

import com.carlosarancibia.playfit.data.remote.RankedSeedGameDto
import com.carlosarancibia.playfit.data.remote.SeedGameDto
import com.carlosarancibia.playfit.model.GameAccessStatus
import com.carlosarancibia.playfit.model.PlatformAvailability
import com.carlosarancibia.playfit.model.ProductConfidence
import org.junit.Assert.assertEquals
import org.junit.Test

class PicksEntityMapperTest {
    @Test
    fun `pick cache round trip preserves recommendation metrics and cautions`() {
        val dto = RankedSeedGameDto(
            game = SeedGameDto(gameId = "game-1", title = "Game One", primaryGenre = "RPG"),
            affinityScore = 82.0,
            riskScore = 37.5,
            confidence = "High",
            fitReasons = listOf("Great combat"),
            cautionReasons = listOf("Long campaign", "Contains || safely"),
            platformAvailability = "Available",
            accessStatus = "Playable",
            inPlayfitPicks = true,
        )

        val cached = dto.toEntity().toDomain()

        assertEquals(82.0, cached.affinityScore, 0.0)
        assertEquals(37.5, cached.riskScore, 0.0)
        assertEquals(ProductConfidence.High, cached.confidence)
        assertEquals(listOf("Long campaign", "Contains || safely"), cached.cautionReasons)
        assertEquals(PlatformAvailability.Available, cached.platformAvailability)
        assertEquals(GameAccessStatus.Playable, cached.accessStatus)
    }
}
