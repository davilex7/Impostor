package com.davilex.impostor.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.davilex.impostor.domain.model.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSettings(
    val mode: GameMode,
    val impostorCount: Int,
    val showHint: Boolean,
    val timerEnabled: Boolean,
    val timerDurationSeconds: Int,
    val selectedCategoryIds: Set<String>
) {
    companion object {
        val DEFAULT = StoredSettings(
            mode = GameMode.CLASICO,
            impostorCount = 1,
            showHint = true,
            timerEnabled = true,
            timerDurationSeconds = 180,
            selectedCategoryIds = emptySet()
        )
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<StoredSettings> = dataStore.data.map { prefs ->
        StoredSettings(
            mode = prefs[SettingsKeys.MODE]
                ?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
                ?: StoredSettings.DEFAULT.mode,
            impostorCount = prefs[SettingsKeys.IMPOSTOR_COUNT] ?: StoredSettings.DEFAULT.impostorCount,
            showHint = prefs[SettingsKeys.SHOW_HINT] ?: StoredSettings.DEFAULT.showHint,
            timerEnabled = prefs[SettingsKeys.TIMER_ENABLED] ?: StoredSettings.DEFAULT.timerEnabled,
            timerDurationSeconds = prefs[SettingsKeys.TIMER_DURATION] ?: StoredSettings.DEFAULT.timerDurationSeconds,
            selectedCategoryIds = prefs[SettingsKeys.SELECTED_CATEGORIES] ?: StoredSettings.DEFAULT.selectedCategoryIds
        )
    }

    suspend fun setMode(mode: GameMode) {
        dataStore.edit { it[SettingsKeys.MODE] = mode.name }
    }

    suspend fun setImpostorCount(count: Int) {
        dataStore.edit { it[SettingsKeys.IMPOSTOR_COUNT] = count }
    }

    suspend fun setShowHint(value: Boolean) {
        dataStore.edit { it[SettingsKeys.SHOW_HINT] = value }
    }

    suspend fun setTimerEnabled(value: Boolean) {
        dataStore.edit { it[SettingsKeys.TIMER_ENABLED] = value }
    }

    suspend fun setTimerDuration(seconds: Int) {
        dataStore.edit { it[SettingsKeys.TIMER_DURATION] = seconds }
    }

    suspend fun setSelectedCategories(ids: Set<String>) {
        dataStore.edit { it[SettingsKeys.SELECTED_CATEGORIES] = ids }
    }
}
