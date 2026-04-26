package com.davilex.impostor.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object SettingsKeys {
    val MODE = stringPreferencesKey("mode")
    val IMPOSTOR_COUNT = intPreferencesKey("impostor_count")
    val SHOW_HINT = booleanPreferencesKey("show_hint")
    val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
    val TIMER_DURATION = intPreferencesKey("timer_duration_seconds")
    val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
}
