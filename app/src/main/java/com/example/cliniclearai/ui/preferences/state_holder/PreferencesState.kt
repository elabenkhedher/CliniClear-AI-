package com.example.cliniclearai.ui.preferences.state_holder

import com.example.cliniclearai.domain.models.UserPreferences

/**
 * État UI immuable pour l'écran des Préférences et Paramètres de l'application.
 *
 * Points clés du cours pour le jury :
 * - UDF (Unidirectional Data Flow) : L'état contient l'ensemble des données nécessaires
 *   à l'affichage de l'écran, y compris les préférences persistées (DataStore),
 *   les statistiques Room (SSOT) et les messages transitoires de confirmation.
 */
data class PreferencesState(
    val preferences: UserPreferences = UserPreferences(),
    val apiKey: String = "",
    val isApiKeySaved: Boolean = false,
    val successMessage: String? = null,
    val isClearing: Boolean = false,
    val reportsCount: Int = 0,
    val drugsCount: Int = 0,
    val symptomsCount: Int = 0
)
