package com.example.cliniclearai.ui.body_map.state_holder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.domain.models.SymptomEntry
import com.example.cliniclearai.ui.common.UiErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Regroupement des champs du formulaire de saisie de symptôme.
 */
private data class FormFields(
    val selectedZone: String,
    val intensity: Int,
    val description: String,
    val associatedFactors: String
)

/**
 * ViewModel pour l'écran Body Map.
 *
 * =========================================================================================
 * POINTS CLÉS DU COURS POUR LA SOUTENANCE DEVANT LE JURY :
 *
 * 1. SSOT (Single Source of Truth) :
 *    - Les symptômes persistés sont extraits en continu de la base Room (`getSymptoms()`).
 *    - Aucune duplication de liste en mémoire : toute insertion ou suppression met à jour Room,
 *      qui émet immédiatement la nouvelle liste immuable vers le StateFlow.
 *
 * 2. UDF (Unidirectional Data Flow) :
 *    - L'état descend vers `BodyMapScreen` via `uiState: StateFlow<BodyMapState>`.
 *    - Les interactions (sélection de zone, slider d'intensité, bouton enregistrer) remontent
 *      via des méthodes explicites.
 *
 * 3. `combine` + `stateIn(WhileSubscribed(5_000))` :
 *    - Combine le flux Room persistant avec les entrées utilisateur réactives.
 *    - La conservation de 5000 ms garantit la fluidité lors de la rotation de l'appareil
 *      sans réinitialiser les champs saisis par le patient.
 * =========================================================================================
 */
@HiltViewModel
class BodyMapViewModel @Inject constructor(
    private val medicalRepository: MedicalRepository
) : ViewModel() {

    // --- États internes modifiables (privés) ---
    private val _selectedZone = MutableStateFlow("")
    private val _intensity = MutableStateFlow(5)
    private val _description = MutableStateFlow("")
    private val _associatedFactors = MutableStateFlow("")
    private val _isSaving = MutableStateFlow(false)
    private val _errorMessages = MutableStateFlow<List<UiErrorMessage>>(emptyList())

    private var lastFailedAction: (() -> Unit)? = null

    private val _formFlow = combine(
        _selectedZone,
        _intensity,
        _description,
        _associatedFactors
    ) { zone, intensity, desc, factors ->
        FormFields(zone, intensity, desc, factors)
    }

    // --- StateFlow public exposé à la vue (SSOT) ---
    val uiState: StateFlow<BodyMapState> = combine(
        medicalRepository.getSymptoms(),
        _formFlow,
        _isSaving,
        _errorMessages
    ) { symptoms, form, isSaving, errorMessages ->
        BodyMapState(
            isLoading = false,
            symptoms = symptoms.sortedByDescending { it.timestamp },
            selectedZone = form.selectedZone,
            intensity = form.intensity,
            description = form.description,
            associatedFactors = form.associatedFactors,
            isSaving = isSaving,
            errorMessages = errorMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BodyMapState(isLoading = true)
    )

    // --- Intentions / Événements UDF ---

    fun onSelectZone(zone: String) {
        _selectedZone.value = if (_selectedZone.value == zone) "" else zone
    }

    fun onIntensityChange(intensity: Int) {
        _intensity.value = intensity.coerceIn(1, 10)
    }

    fun onDescriptionChange(desc: String) {
        _description.value = desc
    }

    fun onAssociatedFactorsChange(factors: String) {
        _associatedFactors.value = factors
    }

    fun saveSymptom() {
        val zone = _selectedZone.value
        val desc = _description.value.trim()

        if (zone.isBlank()) {
            val error = UiErrorMessage(message = "Veuillez sélectionner une zone corporelle sur la carte.")
            _errorMessages.update { it + error }
            return
        }

        if (desc.isBlank()) {
            val error = UiErrorMessage(message = "Veuillez décrire brièvement la sensation ou douleur.")
            _errorMessages.update { it + error }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val newSymptom = SymptomEntry(
                    id = UUID.randomUUID().toString(),
                    bodyPart = zone,
                    intensity = _intensity.value,
                    description = desc,
                    associatedFactors = _associatedFactors.value.trim()
                )
                // Écriture dans Room (SSOT)
                medicalRepository.saveSymptom(newSymptom)

                // Réinitialisation des champs du formulaire
                _selectedZone.value = ""
                _intensity.value = 5
                _description.value = ""
                _associatedFactors.value = ""
            } catch (e: Exception) {
                val error = UiErrorMessage(message = "Erreur lors de l'enregistrement : ${e.localizedMessage}")
                _errorMessages.update { it + error }
                lastFailedAction = { saveSymptom() }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteSymptom(id: String) {
        viewModelScope.launch {
            try {
                medicalRepository.deleteSymptom(id)
            } catch (e: Exception) {
                val error = UiErrorMessage(message = "Erreur lors de la suppression : ${e.localizedMessage}")
                _errorMessages.update { it + error }
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
