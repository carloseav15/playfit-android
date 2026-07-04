package com.carlosarancibia.playfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.ui.PlayfitApp
import com.carlosarancibia.playfit.ui.theme.PlayfitTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authManager: com.carlosarancibia.playfit.data.auth.AuthManager
    @Inject lateinit var preferencesDataStore: PreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager.supabase.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            PlayfitTheme(darkTheme = darkTheme) {
                PlayfitApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        authManager.supabase.handleDeeplinks(intent)
    }
}
