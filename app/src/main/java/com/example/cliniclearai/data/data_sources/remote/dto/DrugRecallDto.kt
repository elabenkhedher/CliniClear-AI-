package com.example.cliniclearai.data.data_sources.remote.dto

import com.google.gson.annotations.SerializedName

data class DrugRecallResponseDto(
    @SerializedName("meta") val meta: MetaDto? = null,
    @SerializedName("results") val results: List<DrugRecallResultDto>? = null
)

data class DrugRecallResultDto(
    @SerializedName("recall_number") val recallNumber: String? = null,
    @SerializedName("product_description") val productDescription: String? = null,
    @SerializedName("reason_for_recall") val reasonForRecall: String? = null,
    @SerializedName("recall_initiation_date") val recallInitiationDate: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("classification") val classification: String? = null,
    @SerializedName("openfda") val openfda: OpenFdaSectionDto? = null
)
