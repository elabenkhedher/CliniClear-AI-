package com.example.cliniclearai.domain.models

data class LabReport(
    val id: String,
    val title: String,
    val date: String,
    val laboratory: String,
    val biomarkers: List<Biomarker> = emptyList(),
    val doctorSummary: String = "",
    val hasAlerts: Boolean = biomarkers.any { it.isAbnormal }
)
