package com.carlosarancibia.playfit.di

import com.carlosarancibia.playfit.data.auth.DeviceIdStore
import com.carlosarancibia.playfit.data.auth.SharedPreferencesDeviceIdStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class IdentityModule {
    @Binds
    @Singleton
    abstract fun bindDeviceIdStore(implementation: SharedPreferencesDeviceIdStore): DeviceIdStore
}
