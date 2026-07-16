package com.carlosarancibia.playfit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ThemeMode
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
        handlePasswordRecoveryIfPresent(intent)
        authManager.supabase.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System.apiValue)
            val darkTheme = when (ThemeMode.fromApiValue(themeMode)) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> isSystemInDarkTheme()
            }
            PlayfitTheme(darkTheme = darkTheme) {
                PlayfitApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePasswordRecoveryIfPresent(intent)
        authManager.supabase.handleDeeplinks(intent)
    }

    /**
     * Supabase's password-recovery deep link is indistinguishable from a normal auth callback once
     * `handleDeeplinks` consumes it, so the raw `type=recovery` marker (query param or fragment) has
     * to be inspected here first and handed to AuthManager before the SDK parses tokens and emits an
     * authenticated session.
     */
    private fun handlePasswordRecoveryIfPresent(intent: Intent) {
        val uri = intent.data ?: return
        authManager.markPendingPasswordRecovery(query = uri.query, fragment = uri.fragment)
    }
}
