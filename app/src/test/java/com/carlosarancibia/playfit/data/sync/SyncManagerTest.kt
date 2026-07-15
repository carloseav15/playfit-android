package com.carlosarancibia.playfit.data.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncManagerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager
    }

    @After
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `enqueueSync replaces a stranded sync request with connected exponential work`() {
        val request = slot<OneTimeWorkRequest>()

        SyncManager(context).enqueueSync()

        verify {
            workManager.enqueueUniqueWork(
                "sync_game_states",
                ExistingWorkPolicy.REPLACE,
                capture(request),
            )
        }
        assertEquals(NetworkType.CONNECTED, request.captured.workSpec.constraints.requiredNetworkType)
        assertEquals(30_000L, request.captured.workSpec.backoffDelayDuration)
    }
}
