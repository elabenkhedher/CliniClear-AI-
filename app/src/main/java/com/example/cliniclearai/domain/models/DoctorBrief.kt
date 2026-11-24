package com.example.cliniclearai.domain.models

data class DoctorBrief(
    val id: String,
    val generatedDate: String,
    val subjective: String,  // S: Symptoms, patient's reported sensations
    val objective: String,   // O: Lab tests, abnormal biomarkers, drugs
    val assessment: String,  // A: Potential interactions, alerts, synthesis
    val plan: String         // P: Prepared questions and talking points for physician
)
