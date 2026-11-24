package com.example.cliniclearai.data.data_sources.remote

import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.data.data_sources.remote.dto.GeminiRequestDto
import com.example.cliniclearai.data.data_sources.remote.dto.GeminiResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit pour l'API Google Gemini (Phase 5).
 * Permet d'analyser le texte brut issu de l'OCR ML Kit et de le transformer
 * en rapport médical structuré (JSON).
 */
interface AiAnalysisApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = Constants.DEFAULT_GEMINI_MODEL,
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): GeminiResponseDto
}
