package com.example.cliniclearai.data.data_sources.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.cliniclearai.domain.models.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val SHOW_ONLY_ALERTS = booleanPreferencesKey("show_only_alerts")
        val PREFERRED_UNITS = stringPreferencesKey("preferred_units")
        val EXPLANATION_LANGUAGE = stringPreferencesKey("explanation_language")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                showOnlyAlerts = preferences[PreferencesKeys.SHOW_ONLY_ALERTS] ?: false,
                preferredUnits = preferences[PreferencesKeys.PREFERRED_UNITS] ?: "STANDARD",
                explanationLanguage = preferences[PreferencesKeys.EXPLANATION_LANGUAGE] ?: "fr",
                geminiApiKey = preferences[PreferencesKeys.GEMINI_API_KEY] ?: ""
            )
        }

    suspend fun updateShowOnlyAlerts(showOnlyAlerts: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ONLY_ALERTS] = showOnlyAlerts
        }
    }

    suspend fun updatePreferredUnits(preferredUnits: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_UNITS] = preferredUnits
        }
    }

    suspend fun updateExplanationLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPLANATION_LANGUAGE] = language
        }
    }

    suspend fun updateGeminiApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = apiKey
        }
    }
}
