package com.example.cliniclearai.domain.use_cases

import java.util.UUID

enum class AlertType {
    DUPLICATE_INGREDIENT,
    DRUG_INTERACTION,
    ACTIVE_RECALL
}

data class DrugAlert(
    val id: String = UUID.randomUUID().toString(),
    val type: AlertType = AlertType.DRUG_INTERACTION,
    val title: String,
    val description: String,
    val involvedDrugs: List<String> = emptyList()
)
