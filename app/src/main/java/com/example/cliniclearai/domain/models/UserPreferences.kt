package com.example.cliniclearai.domain.models

data class UserPreferences(
    val showOnlyAlerts: Boolean = false,
    val preferredUnits: String = "STANDARD", // STANDARD vs METRIC / SI
    val explanationLanguage: String = "fr",
    val geminiApiKey: String = ""
)
