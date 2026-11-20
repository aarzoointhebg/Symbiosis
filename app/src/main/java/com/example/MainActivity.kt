package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AccessibilityPreferencesRepository
import com.example.data.configureAppCheck
import com.example.model.AccessibilitySettings
import com.example.ui.SymbiosisApp
import com.example.ui.theme.SymbiosisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // App Check MUST be installed before any Firebase AI Logic / Firestore calls or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val accessibilityRepo = AccessibilityPreferencesRepository(applicationContext)

        setContent {
            val accessibilitySettings by accessibilityRepo.settingsFlow.collectAsStateWithLifecycle(
                initialValue = AccessibilitySettings()
            )
            SymbiosisTheme(
                themeMode = accessibilitySettings.themeMode,
                highContrast = accessibilitySettings.highContrastMode,
                textScale = accessibilitySettings.textScale,
                reducedMotion = accessibilitySettings.reducedMotion
            ) {
                SymbiosisApp(
                    accessibilityRepo = accessibilityRepo,
                    accessibilitySettings = accessibilitySettings
                )
            }
        }
    }
}
