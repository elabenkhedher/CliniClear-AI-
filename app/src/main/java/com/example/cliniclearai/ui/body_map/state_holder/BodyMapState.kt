package com.example.cliniclearai.ui.body_map.state_holder

import com.example.cliniclearai.domain.models.SymptomEntry
import com.example.cliniclearai.ui.common.UiErrorMessage

/**
 * Zones corporelles prédéfinies pour le Body Map clinique.
 */
object BodyZones {
    val ALL = listOf(
        "Tête & Crâne",
        "Gorge & Cou",
        "Thorax & Poitrine",
        "Abdomen & Estomac",
        "Dos & Rachis",
        "Épaules & Bras",
        "Bassin & Hanches",
        "Jambes & Genoux",
        "Chevilles & Pieds"
    )
}

/**
 * État UI immuable pour l'écran Body Map (localisation corporelle des symptômes).
 *
 * Points clés du cours pour le jury :
 * - SSOT : La liste [symptoms] provient de Room via le Repository.
 * - UDF : Les champs de saisie ([selectedZone], [intensity], [description]) font partie
 *   de l'état immuable et sont mis à jour par des événements unidirectionnels.
 * - errorMessages : Liste immuable d'erreurs pour l'affichage Snackbar.
 */
data class BodyMapState(
    val isLoading: Boolean = false,
    val symptoms: List<SymptomEntry> = emptyList(),
    val selectedZone: String = "",
    val intensity: Int = 5,
    val description: String = "",
    val associatedFactors: String = "",
    val isSaving: Boolean = false,
    val errorMessages: List<UiErrorMessage> = emptyList()
)
