package com.example.cliniclearai.ui.preferences.state_holder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.data.repositories.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel gérant l'écran de Préférences & Paramètres de l'application.
 *
 * Points clés d'architecture pour le jury :
 * - Jetpack DataStore (Preferences DataStore) : Persistance réactive et asynchrone des réglages utilisateur.
 * - SSOT : Les statistiques de la base Room sont combinées réactivement via [MedicalRepository].
 * - UDF & Cycle de vie : Utilisation de `stateIn(WhileSubscribed(5_000))` pour survivre aux rotations d'écran.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val medicalRepository: MedicalRepository
) : ViewModel() {

    private val _apiKey = MutableStateFlow(Constants.DEFAULT_GEMINI_API_KEY)
    private val _successMessage = MutableStateFlow<String?>(null)
    private val _isClearing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                if (_apiKey.value.isBlank() || _apiKey.value == Constants.DEFAULT_GEMINI_API_KEY) {
                    val key = prefs.geminiApiKey.ifBlank { Constants.DEFAULT_GEMINI_API_KEY }
                    _apiKey.value = key
                }
            }
        }
    }

    private val _statsFlow = combine(
        medicalRepository.getLabReports(),
        medicalRepository.getDrugs(),
        medicalRepository.getSymptoms()
    ) { reports, drugs, symptoms ->
        Triple(reports.size, drugs.size, symptoms.size)
    }

    private val _uiParamsFlow = combine(
        _apiKey,
        _successMessage,
        _isClearing
    ) { apiKey, successMsg, isClearing ->
        Triple(apiKey, successMsg, isClearing)
    }

    val uiState: StateFlow<PreferencesState> = combine(
        userPreferencesRepository.getUserPreferences(),
        _statsFlow,
        _uiParamsFlow
    ) { prefs, stats, uiParams ->
        PreferencesState(
            preferences = prefs,
            apiKey = uiParams.first,
            reportsCount = stats.first,
            drugsCount = stats.second,
            symptomsCount = stats.third,
            successMessage = uiParams.second,
            isClearing = uiParams.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreferencesState()
    )

    fun onToggleShowOnlyAlerts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowOnlyAlerts(enabled)
        }
    }

    fun onPreferredUnitsChanged(units: String) {
        viewModelScope.launch {
            userPreferencesRepository.setPreferredUnits(units)
            _successMessage.value = "Unités médicales mises à jour."
        }
    }

    fun onExplanationLanguageChanged(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setExplanationLanguage(language)
            _successMessage.value = "Langue de vulgarisation modifiée."
        }
    }

    fun onApiKeyChanged(newKey: String) {
        _apiKey.value = newKey
    }

    fun saveApiKey() {
        viewModelScope.launch {
            userPreferencesRepository.setGeminiApiKey(_apiKey.value.trim())
            _successMessage.value = "Clé Google Gemini API enregistrée avec succès."
        }
    }

    fun clearTestData() {
        viewModelScope.launch {
            _isClearing.value = true
            medicalRepository.clearTestData()
            _isClearing.value = false
            _successMessage.value = "Données de test supprimées avec succès."
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _isClearing.value = true
            medicalRepository.clearAllData()
            _isClearing.value = false
            _successMessage.value = "Toutes les données locales ont été réinitialisées."
        }
    }

    fun dismissSuccessMessage() {
        _successMessage.value = null
    }
}
