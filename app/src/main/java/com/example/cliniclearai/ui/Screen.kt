package com.example.cliniclearai.ui

import com.example.cliniclearai.common.Constants

/**
 * Définition scellée des routes de navigation de l'application CliniClear AI.
 *
 * Points clés du cours pour le jury :
 * - Sécurisation du typage de la navigation Compose : chaque destination possède une route
 *   normalisée avec gestion propre des arguments d'URL.
 */
sealed class Screen(val route: String) {
    object ReportList : Screen("report_list")
    object ReportDetail : Screen("report_detail/{${Constants.PARAM_REPORT_ID}}") {
        fun createRoute(reportId: String): String = "report_detail/$reportId"
    }
    object BodyMap : Screen("body_map")
    object DoctorBrief : Screen("doctor_brief")
    object Preferences : Screen("preferences")
}
