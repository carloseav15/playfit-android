package com.carlosarancibia.playfit.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesDataStoreTest {
    @Test
    fun `selected platform ids round trip through JSON including commas`() {
        val ids = setOf("switch", "pc,handheld", "ps5")

        val encoded = encodeSelectedPlatformIds(ids)

        assertEquals(ids, decodeSelectedPlatformIds(encoded))
    }

    @Test
    fun `selected platform ids still reads legacy CSV`() {
        assertEquals(
            setOf("switch", "ps5"),
            decodeSelectedPlatformIds("switch,ps5"),
        )
    }

    @Test
    fun `selected platform ids treats missing or malformed data as empty`() {
        assertEquals(emptySet<String>(), decodeSelectedPlatformIds(null))
        assertEquals(emptySet<String>(), decodeSelectedPlatformIds("[not valid json"))
    }
}
