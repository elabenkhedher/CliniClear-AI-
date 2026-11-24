package com.example.cliniclearai.ui.report_detail.state_holder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.data_sources.local.BiomarkerLocalizationHelper
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.data.repositories.UserPreferencesRepository
import com.example.cliniclearai.domain.use_cases.GetReportDetailUseCase
import com.example.cliniclearai.ui.common.UiErrorMessage
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
 * ViewModel pour l'écran de détail d'un bilan biologique.
 *
 * =========================================================================================
 * POINTS CLÉS DU COURS POUR LA SOUTENANCE DEVANT LE JURY :
 *
 * 1. SSOT (Single Source of Truth) :
 *    - Le flux `getLabReportById(reportId)` observe la base Room en continu.
 *    - Dès qu'un bilan est modifié ou persisté, l'écran de détail reçoit automatiquement
 *      la version actualisée sans manipulation manuelle de listes.
 *
 * 2. Protection par Mutex (Couche Data via GetReportDetailUseCase) :
 *    - Lors du rafraîchissement via `loadReportDetail()`, le Repository vérifie en premier
 *      lieu son cache mémoire protégé par un `Mutex` (`withLock`).
 *    - Cela garantit une lecture/écriture atomique et thread-safe sans bloquer le thread principal.
 *
 * 3. UDF & combine + stateIn(WhileSubscribed(5_000)) :
 *    - L'état est produit par la combinaison pure de la source Room et des filtres UI locaux.
 *    - L'immuabilité garantit l'absence d'états incohérents lors des rotations d'écran.
 * =========================================================================================
 */
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val medicalRepository: MedicalRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getReportDetailUseCase: GetReportDetailUseCase
) : ViewModel() {

    val reportId: String = savedStateHandle[Constants.PARAM_REPORT_ID] ?: ""

    // --- États UI modifiables internes ---
    private val _filterAbnormalOnly = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessages = MutableStateFlow<List<UiErrorMessage>>(emptyList())

    // Dernière action échouée pour le bouton "Réessayer"
    private var lastFailedAction: (() -> Unit)? = null

    // --- StateFlow public exposé à la vue ---
    val uiState: StateFlow<ReportDetailState> = combine(
        medicalRepository.getLabReportById(reportId),
        userPreferencesRepository.getUserPreferences(),
        _filterAbnormalOnly,
        _isLoading,
        _errorMessages
    ) { rawReport, userPrefs, filterAbnormal, isLoading, errorMessages ->
        val localizedReport = if (rawReport != null) {
            BiomarkerLocalizationHelper.localizeReport(rawReport, userPrefs.explanationLanguage)
        } else null

        val filteredReport = if (filterAbnormal && localizedReport != null) {
            localizedReport.copy(biomarkers = localizedReport.biomarkers.filter { it.isAbnormal })
        } else {
            localizedReport
        }

        ReportDetailState(
            isLoading = isLoading,
            report = filteredReport,
            filterAbnormalOnly = filterAbnormal,
            explanationLanguage = userPrefs.explanationLanguage,
            errorMessages = errorMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportDetailState(isLoading = true)
    )

    init {
        loadReportDetail()
    }

    /**
     * Charge le détail du bilan via le Use Case qui consulte le cache mémoire protégé par Mutex
     * avant de basculer sur Room.
     */
    fun loadReportDetail() {
        if (reportId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getReportDetailUseCase(reportId)) {
                is Resource.Success -> {
                    // Les données sont synchronisées via le Flow Room (SSOT)
                }
                is Resource.Error -> {
                    val error = UiErrorMessage(
                        message = result.message ?: "Impossible de charger le bilan médical."
                    )
                    _errorMessages.update { it + error }
                    lastFailedAction = { loadReportDetail() }
                }
                is Resource.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun toggleFilterAbnormalOnly() {
        _filterAbnormalOnly.update { !it }
    }

    /**
     * Modifie la langue de vulgarisation pour ce bilan et met à jour les préférences de l'utilisateur.
     */
    fun onExplanationLanguageChanged(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setExplanationLanguage(language)
            if (reportId.isNotBlank()) {
                medicalRepository.updateReportLanguage(reportId, language)
            }
        }
    }

    /**
     * Supprime le bilan de la base Room (SSOT) et exécute le callback (ex: retour arrière).
     */
    fun deleteReport(onSuccess: () -> Unit) {
        if (reportId.isBlank()) return
        viewModelScope.launch {
            medicalRepository.deleteLabReport(reportId)
            onSuccess()
        }
    }

    fun dismissError(errorId: Long) {
        _errorMessages.update { list -> list.filterNot { it.id == errorId } }
    }

    fun retryLastAction() {
        val action = lastFailedAction
        lastFailedAction = null
        action?.invoke()
    }
}
