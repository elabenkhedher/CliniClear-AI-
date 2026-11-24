package com.example.cliniclearai.ui.doctor_brief.state_holder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cliniclearai.domain.models.DoctorBrief
import com.example.cliniclearai.domain.use_cases.GenerateDoctorBriefUseCase
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
 * ViewModel pour l'écran de synthèse médicale au format SOAP (Doctor Brief).
 *
 * =========================================================================================
 * POINTS CLÉS DU COURS POUR LA SOUTENANCE DEVANT LE JURY :
 *
 * 1. SSOT & Agrégation multi-sources :
 *    - Le Use Case `GenerateDoctorBriefUseCase` interroge la base Room (symptômes, bilans,
 *      médicaments) pour produire une synthèse cohérente sans désynchronisation.
 *
 * 2. UDF (Unidirectional Data Flow) :
 *    - `DoctorBriefState` est immuable et expose le résultat au Composable.
 *    - Les actions (actualiser, partager) sont déclenchées par l'UI et traitées de façon
 *      sécurisée dans le `viewModelScope`.
 *
 * 3. `combine` + `stateIn(WhileSubscribed(5_000))` :
 *    - Maintient la fiche générée en mémoire vive lors des rotations d'écran
 *      sans forcer un recalcul coûteux des chaînes de caractères et de l'analyse SOAP.
 * =========================================================================================
 */
@HiltViewModel
class DoctorBriefViewModel @Inject constructor(
    private val generateDoctorBriefUseCase: GenerateDoctorBriefUseCase
) : ViewModel() {

    private val _brief = MutableStateFlow<DoctorBrief?>(null)
    private val _isGenerating = MutableStateFlow(false)
    private val _errorMessages = MutableStateFlow<List<UiErrorMessage>>(emptyList())

    private var lastFailedAction: (() -> Unit)? = null

    // StateFlow exposé à la vue via combine + stateIn
    val uiState: StateFlow<DoctorBriefState> = combine(
        _brief,
        _isGenerating,
        _errorMessages
    ) { brief, isGenerating, errorMessages ->
        DoctorBriefState(
            isLoading = isGenerating && brief == null,
            isGenerating = isGenerating,
            brief = brief,
            errorMessages = errorMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DoctorBriefState(isLoading = true)
    )

    init {
        generateBrief()
    }

    /**
     * Génère la fiche SOAP en agrégeant de manière asynchrone toutes les données cliniques.
     */
    fun generateBrief() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val brief = generateDoctorBriefUseCase()
                _brief.value = brief
            } catch (e: Exception) {
                val error = UiErrorMessage(
                    message = "Erreur lors de la génération de la synthèse clinique : ${e.localizedMessage}"
                )
                _errorMessages.update { it + error }
                lastFailedAction = { generateBrief() }
            } finally {
                _isGenerating.value = false
            }
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
