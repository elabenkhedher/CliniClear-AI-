package com.example.cliniclearai.domain.models

data class DrugRecall(
    val recallNumber: String,
    val productDescription: String,
    val reasonForRecall: String,
    val recallInitiationDate: String,
    val status: String,
    val classification: String = "",
    val brandName: String = "",
    val genericName: String = ""
)
