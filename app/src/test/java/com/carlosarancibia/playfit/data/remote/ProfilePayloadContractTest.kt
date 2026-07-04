package com.carlosarancibia.playfit.data.remote

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePayloadContractTest {
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun `profile payload matches strict web API shape`() {
        val request = ProfileSaveRequest(
            deviceId = "device-1",
            gameStates = emptyMap(),
            profile = ProfileDto(),
            onboarding = PersistedOnboardingDto(
                step = "dislikes",
                platforms = listOf(PlatformSelectionDto("ps5", "available")),
                likedGameIds = listOf("a", "b", "c"),
                dislikedGameIds = listOf("miss"),
                onboardingCompletedAt = "2026-07-02T00:00:00Z",
            ),
        )

        val encoded = json.parseToJsonElement(json.encodeToString(request)).jsonObject

        assertEquals("device-1", encoded.getValue("deviceId").jsonPrimitive.content)
        assertFalse("device_id" in encoded)
        assertTrue(encoded.getValue("gameStates").jsonObject.isEmpty())
        assertTrue("profile" in encoded)
        val onboarding = encoded.getValue("onboarding").jsonObject
        assertEquals("dislikes", onboarding.getValue("step").jsonPrimitive.content)
        assertEquals(3, onboarding.getValue("likedGameIds").jsonArray.size)
        val platform = onboarding.getValue("platforms").jsonArray.single().jsonObject
        assertEquals("ps5", platform.getValue("platformId").jsonPrimitive.content)
        assertEquals("available", platform.getValue("status").jsonPrimitive.content)
    }

    @Test
    fun `optional game state fields are omitted instead of encoded as null`() {
        val encoded = json.parseToJsonElement(
            json.encodeToString(GameStateDto(gameId = "game-1")),
        ).jsonObject

        assertFalse("status" in encoded)
        assertFalse("rating" in encoded)
        assertFalse("excluded" in encoded)
        assertEquals("manual", encoded.getValue("source").jsonPrimitive.content)
    }
}
