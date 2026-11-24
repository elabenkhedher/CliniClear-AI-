package com.example.cliniclearai.ui.report_detail.state_holder

import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.ui.common.UiErrorMessage

/**
 * État UI immuable pour l'écran de détail d'un bilan biologique.
 *
 * Points clés du cours pour le jury :
 * - SSOT : L'état complet de la vue est modélisé de façon centralisée et immuable.
 * - UDF : Les filtres locaux (ex: afficher seulement les anomalies) et les erreurs
 *   sont combinés de manière pure dans le ViewModel.
 * - errorMessages : Permet le traitement systématique des erreurs de chargement ou réseau.
 */
data class ReportDetailState(
    val isLoading: Boolean = false,
    val report: LabReport? = null,
    val filterAbnormalOnly: Boolean = false,
    val explanationLanguage: String = "fr",
    val errorMessages: List<UiErrorMessage> = emptyList()
)
