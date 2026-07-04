package com.carlosarancibia.playfit.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIdProviderTest {

    @Test
    fun `existing device id survives provider recreation`() {
        val existing = "d9428888-122b-11e1-b85c-61cd3cbb3210"
        val store = InMemoryDeviceIdStore(existing)

        val first = DeviceIdProvider(store).id
        val second = DeviceIdProvider(store).id

        assertEquals(existing, first)
        assertEquals(first, second)
        assertEquals(0, store.writeCount)
    }

    @Test
    fun `missing device id is generated once and persisted`() {
        val store = InMemoryDeviceIdStore()

        val generated = DeviceIdProvider(store).id

        assertTrue(DeviceIdProvider.isValidDeviceId(generated))
        assertEquals(generated, store.value)
        assertEquals(1, store.writeCount)
        assertEquals(generated, DeviceIdProvider(store).id)
        assertEquals(1, store.writeCount)
    }

    @Test
    fun `invalid stored device id is replaced`() {
        val store = InMemoryDeviceIdStore("not-a-uuid")

        val generated = DeviceIdProvider(store).id

        assertNotEquals("not-a-uuid", generated)
        assertTrue(DeviceIdProvider.isValidDeviceId(generated))
        assertEquals(generated, store.value)
    }

    private class InMemoryDeviceIdStore(
        var value: String? = null,
    ) : DeviceIdStore {
        var writeCount = 0

        override fun read(): String? = value

        override fun write(value: String) {
            this.value = value
            writeCount += 1
        }
    }
}
