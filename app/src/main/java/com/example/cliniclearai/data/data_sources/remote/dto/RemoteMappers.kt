package com.example.cliniclearai.data.data_sources.remote.dto

import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.DrugRecall
import java.util.UUID

fun DrugLabelResultDto.toDomain(): Drug {
    val brand = openfda?.brandName?.firstOrNull() ?: "Médicament inconnu"
    val generic = openfda?.genericName?.firstOrNull() ?: brand
    val activeSubstances = openfda?.substanceName
        ?: activeIngredient
        ?: listOf(generic)
    val isPrescription = openfda?.productType?.any { it.contains("PRESCRIPTION", ignoreCase = true) } ?: true

    return Drug(
        id = id ?: UUID.randomUUID().toString(),
        brandName = brand,
        genericName = generic,
        activeIngredients = activeSubstances,
        purpose = purpose?.joinToString(separator = "\n") ?: "",
        warnings = warnings?.take(3)?.joinToString(separator = "\n") ?: "",
        instructions = dosageAndAdministration?.firstOrNull() ?: "",
        isPrescription = isPrescription
    )
}

fun DrugRecallResultDto.toDomain(): DrugRecall {
    return DrugRecall(
        recallNumber = recallNumber ?: "N/A",
        productDescription = productDescription ?: "",
        reasonForRecall = reasonForRecall ?: "Raison non spécifiée",
        recallInitiationDate = recallInitiationDate ?: "",
        status = status ?: "Ongoing",
        classification = classification ?: "",
        brandName = openfda?.brandName?.firstOrNull() ?: "",
        genericName = openfda?.genericName?.firstOrNull() ?: ""
    )
}
