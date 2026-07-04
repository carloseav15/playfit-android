package com.carlosarancibia.playfit.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(
                backoffDelay = 30,
                backoffPolicy = androidx.work.BackoffPolicy.EXPONENTIAL,
                timeUnit = TimeUnit.SECONDS,
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
    }

    fun cancelSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    }

    companion object {
        private const val SYNC_WORK_NAME = "sync_game_states"
    }
}
