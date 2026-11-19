package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AccessibilitySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.accessibilityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "symbiosis_accessibility"
)

class AccessibilityPreferencesRepository(private val context: Context) {
    private object Keys {
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val PREFERRED_COMMS = stringPreferencesKey("preferred_comms")
    }

    val settingsFlow: Flow<AccessibilitySettings> = context.accessibilityDataStore.data.map { prefs ->
        AccessibilitySettings(
            textScale = prefs[Keys.TEXT_SCALE] ?: 1.0f,
            highContrastMode = prefs[Keys.HIGH_CONTRAST] ?: false,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            preferredComms = prefs[Keys.PREFERRED_COMMS] ?: "Text & Screen-Reader Structured"
        )
    }

    suspend fun setTextScale(scale: Float) {
        context.accessibilityDataStore.edit { it[Keys.TEXT_SCALE] = scale.coerceIn(0.85f, 1.4f) }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.accessibilityDataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.accessibilityDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setPreferredComms(comms: String) {
        context.accessibilityDataStore.edit { it[Keys.PREFERRED_COMMS] = comms }
    }
}
