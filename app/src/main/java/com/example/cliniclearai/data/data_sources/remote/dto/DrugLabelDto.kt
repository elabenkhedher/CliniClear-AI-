package com.example.cliniclearai.data.data_sources.remote.dto

import com.google.gson.annotations.SerializedName

data class DrugLabelResponseDto(
    @SerializedName("meta") val meta: MetaDto? = null,
    @SerializedName("results") val results: List<DrugLabelResultDto>? = null
)

data class MetaDto(
    @SerializedName("disclaimer") val disclaimer: String? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null
)

data class DrugLabelResultDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("purpose") val purpose: List<String>? = null,
    @SerializedName("indications_and_usage") val indicationsAndUsage: List<String>? = null,
    @SerializedName("warnings") val warnings: List<String>? = null,
    @SerializedName("dosage_and_administration") val dosageAndAdministration: List<String>? = null,
    @SerializedName("active_ingredient") val activeIngredient: List<String>? = null,
    @SerializedName("openfda") val openfda: OpenFdaSectionDto? = null
)

data class OpenFdaSectionDto(
    @SerializedName("brand_name") val brandName: List<String>? = null,
    @SerializedName("generic_name") val genericName: List<String>? = null,
    @SerializedName("substance_name") val substanceName: List<String>? = null,
    @SerializedName("product_type") val productType: List<String>? = null,
    @SerializedName("manufacturer_name") val manufacturerName: List<String>? = null,
    @SerializedName("route") val route: List<String>? = null
)
