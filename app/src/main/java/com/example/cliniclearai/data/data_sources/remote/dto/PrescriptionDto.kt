package com.example.cliniclearai.data.data_sources.remote.dto

import com.example.cliniclearai.domain.models.Drug
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class PrescriptionResponseDto(
    @SerializedName("doctorName") val doctorName: String? = null,
    @SerializedName("prescriptionDate") val prescriptionDate: String? = null,
    @SerializedName("drugs") val drugs: List<PrescriptionDrugDto>? = null
)

data class PrescriptionDrugDto(
    @SerializedName("brandName") val brandName: String? = null,
    @SerializedName("genericName") val genericName: String? = null,
    @SerializedName("activeIngredients") val activeIngredients: List<String>? = null,
    @SerializedName("dosage") val dosage: String? = null,
    @SerializedName("frequency") val frequency: String? = null,
    @SerializedName("instructions") val instructions: String? = null,
    @SerializedName("purpose") val purpose: String? = null,
    @SerializedName("warnings") val warnings: String? = null,
    @SerializedName("isPrescription") val isPrescription: Boolean? = true
)

fun PrescriptionDrugDto.toDomain(): Drug {
    val bName = brandName?.trim()?.ifBlank { "Médicament" } ?: "Médicament"
    val gName = genericName?.trim()?.ifBlank { bName } ?: bName
    val ingredients = if (!activeIngredients.isNullOrEmpty()) {
        activeIngredients.map { it.trim().lowercase() }.filter { it.isNotBlank() }
    } else {
        listOf(gName.lowercase())
    }

    return Drug(
        id = "drug_${UUID.randomUUID().toString().take(8)}",
        brandName = bName,
        genericName = gName,
        activeIngredients = ingredients,
        dosage = dosage?.trim() ?: "",
        frequency = frequency?.trim() ?: "Selon ordonnance",
        instructions = instructions?.trim() ?: "",
        purpose = purpose?.trim() ?: "Prescription médicale",
        warnings = warnings?.trim() ?: "",
        hasActiveRecall = false,
        isPrescription = isPrescription ?: true
    )
}
