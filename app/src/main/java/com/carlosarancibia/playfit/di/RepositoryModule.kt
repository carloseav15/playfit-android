package com.carlosarancibia.playfit.di

import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.local.PlayfitDatabase
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.data.remote.PlayfitApiService
import com.carlosarancibia.playfit.data.repository.PlayfitRepositoryImpl
import com.carlosarancibia.playfit.data.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePlayfitRepository(
        apiService: PlayfitApiService,
        database: PlayfitDatabase,
        preferencesDataStore: PreferencesDataStore,
        authManager: AuthManager,
        syncManager: SyncManager,
    ): PlayfitRepository {
        return PlayfitRepositoryImpl(
            apiService = apiService,
            database = database,
            preferencesDataStore = preferencesDataStore,
            authManager = authManager,
            syncManager = syncManager,
        )
    }
}
