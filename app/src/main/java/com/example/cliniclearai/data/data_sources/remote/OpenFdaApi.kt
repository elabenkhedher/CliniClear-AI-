package com.example.cliniclearai.data.data_sources.remote

import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.data.data_sources.remote.dto.DrugLabelResponseDto
import com.example.cliniclearai.data.data_sources.remote.dto.DrugRecallResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    @GET(Constants.DRUG_LABEL_ENDPOINT)
    suspend fun searchDrugLabels(
        @Query("search") searchQuery: String,
        @Query("limit") limit: Int = Constants.DEFAULT_API_LIMIT
    ): DrugLabelResponseDto

    @GET(Constants.DRUG_ENFORCEMENT_ENDPOINT)
    suspend fun getDrugRecalls(
        @Query("search") searchQuery: String = "status:Ongoing",
        @Query("limit") limit: Int = Constants.DEFAULT_API_LIMIT
    ): DrugRecallResponseDto
}
