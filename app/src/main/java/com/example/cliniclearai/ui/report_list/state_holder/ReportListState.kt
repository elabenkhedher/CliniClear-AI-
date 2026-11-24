package com.example.cliniclearai.ui.report_list.state_holder

import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.DrugAlert
import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.ui.common.UiErrorMessage

/**
 * Onglets disponibles sur le tableau de bord principal.
 */
enum class ReportListTab {
    REPORTS,
    DRUGS_AND_ALERTS
}

/**
 * État UI immuable pour l'écran de liste des bilans et médicaments.
 *
 * Points clés du cours pour le jury :
 * - SSOT (Single Source of Truth) côté UI : Toute la vue dépend d'une unique data class
 *   immuable. Impossible pour un composant graphique d'avoir un état désynchronisé.
 * - UDF (Unidirectional Data Flow) : L'état descend vers les Composables. Toute action
 *   utilisateur remonte au ViewModel via des événements / méthodes.
 * - errorMessages : Liste immuable d'erreurs (réseau OpenFDA, etc.) permettant un affichage
 *   séquentiel fiable dans un Snackbar sans perte d'événements.
 * - isScanning : Indicateur d'état pendant l'OCR ML Kit et l'analyse Gemini.
 */
data class ReportListState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val reports: List<LabReport> = emptyList(),
    val drugs: List<Drug> = emptyList(),
    val drugAlerts: List<DrugAlert> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: ReportListTab = ReportListTab.REPORTS,
    val showOnlyAlerts: Boolean = false,
    val isCheckingRecalls: Boolean = false,
    val errorMessages: List<UiErrorMessage> = emptyList(),
    val lastScannedReport: LabReport? = null,
    val lastScannedPrescriptionDrugs: List<Drug>? = null
)
