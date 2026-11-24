package com.example.cliniclearai.ui.doctor_brief.state_holder

import com.example.cliniclearai.domain.models.DoctorBrief
import com.example.cliniclearai.ui.common.UiErrorMessage

/**
 * État UI immuable pour l'écran Doctor Brief (synthèse clinique au format SOAP).
 *
 * Points clés du cours pour le jury :
 * - SSOT : Résulte de l'agrégation ordonnée des sources Room (bilans, médicaments, symptômes).
 * - UDF : L'état contient la fiche [brief] complète ainsi que les indicateurs [isGenerating]
 *   et la liste [errorMessages].
 */
data class DoctorBriefState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val brief: DoctorBrief? = null,
    val errorMessages: List<UiErrorMessage> = emptyList()
)
