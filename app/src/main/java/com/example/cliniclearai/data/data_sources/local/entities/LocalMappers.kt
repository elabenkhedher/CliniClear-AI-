package com.example.cliniclearai.data.data_sources.local.entities

import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.domain.models.SymptomEntry

// LabReport mapping
fun LabReportEntity.toDomain(): LabReport = LabReport(
    id = id,
    title = title,
    date = date,
    laboratory = laboratory,
    biomarkers = biomarkers,
    doctorSummary = doctorSummary,
    hasAlerts = hasAlerts
)

fun LabReport.toEntity(): LabReportEntity = LabReportEntity(
    id = id,
    title = title,
    date = date,
    laboratory = laboratory,
    biomarkers = biomarkers,
    doctorSummary = doctorSummary,
    hasAlerts = hasAlerts
)

// Drug mapping
fun DrugEntity.toDomain(): Drug = Drug(
    id = id,
    brandName = brandName,
    genericName = genericName,
    activeIngredients = activeIngredients,
    dosage = dosage,
    frequency = frequency,
    instructions = instructions,
    purpose = purpose,
    warnings = warnings,
    hasActiveRecall = hasActiveRecall,
    isPrescription = isPrescription
)

fun Drug.toEntity(): DrugEntity = DrugEntity(
    id = id,
    brandName = brandName,
    genericName = genericName,
    activeIngredients = activeIngredients,
    dosage = dosage,
    frequency = frequency,
    instructions = instructions,
    purpose = purpose,
    warnings = warnings,
    hasActiveRecall = hasActiveRecall,
    isPrescription = isPrescription
)

// SymptomEntry mapping
fun SymptomEntity.toDomain(): SymptomEntry = SymptomEntry(
    id = id,
    bodyPart = bodyPart,
    intensity = intensity,
    description = description,
    timestamp = timestamp,
    associatedFactors = associatedFactors
)

fun SymptomEntry.toEntity(): SymptomEntity = SymptomEntity(
    id = id,
    bodyPart = bodyPart,
    intensity = intensity,
    description = description,
    timestamp = timestamp,
    associatedFactors = associatedFactors
)
