package com.example.cliniclearai.domain.models

typealias DrugAlert = com.example.cliniclearai.domain.use_cases.DrugAlert
typealias AlertType = com.example.cliniclearai.domain.use_cases.AlertType

enum class InteractionSeverity {
    INFO,
    MODERATE,
    HIGH,
    CRITICAL
}

data class DrugInteractionAlert(
    val severity: InteractionSeverity,
    val title: String,
    val description: String,
    val involvedDrugs: List<String> = emptyList(),
    val recommendation: String = ""
)
