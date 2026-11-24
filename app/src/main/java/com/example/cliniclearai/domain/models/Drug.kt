package com.example.cliniclearai.domain.models

data class Drug(
    val id: String,
    val brandName: String,
    val genericName: String,
    val activeIngredients: List<String> = emptyList(),
    val dosage: String = "",
    val frequency: String = "",
    val instructions: String = "",
    val purpose: String = "",
    val warnings: String = "",
    val hasActiveRecall: Boolean = false,
    val isPrescription: Boolean = true
)
