package com.example.cliniclearai.domain.models

data class SymptomEntry(
    val id: String,
    val bodyPart: String,
    val intensity: Int, // 1 - 10
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val associatedFactors: String = ""
)
