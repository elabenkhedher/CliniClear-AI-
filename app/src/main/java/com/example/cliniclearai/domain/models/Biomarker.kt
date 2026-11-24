package com.example.cliniclearai.domain.models

enum class BiomarkerStatus {
    NORMAL,
    LOW,
    HIGH,
    CRITICAL
}

data class Biomarker(
    val id: String,
    val name: String,
    val value: Double,
    val unit: String,
    val minReference: Double,
    val maxReference: Double,
    val status: BiomarkerStatus = BiomarkerStatus.NORMAL,
    val vulgarization: String = "",
    val recommendation: String = ""
) {
    val isAbnormal: Boolean
        get() = status != BiomarkerStatus.NORMAL
}
