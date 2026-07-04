package com.carlosarancibia.playfit.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

internal interface DeviceIdStore {
    fun read(): String?
    fun write(value: String)
}

@Singleton
internal class SharedPreferencesDeviceIdStore @Inject constructor(
    @ApplicationContext context: Context,
) : DeviceIdStore {
    private val preferences = context.getSharedPreferences("playfit_identity", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY_DEVICE_ID, null)

    override fun write(value: String) {
        preferences.edit().putString(KEY_DEVICE_ID, value).apply()
    }

    private companion object {
        const val KEY_DEVICE_ID = "device_id"
    }
}

@Singleton
class DeviceIdProvider @Inject internal constructor(
    private val store: DeviceIdStore,
) {
    val id: String by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        store.read()
            ?.takeIf(::isValidDeviceId)
            ?: UUID.randomUUID().toString().lowercase().also(store::write)
    }

    companion object {
        fun isValidDeviceId(value: String): Boolean {
            return runCatching { UUID.fromString(value) }.isSuccess
        }
    }
}
