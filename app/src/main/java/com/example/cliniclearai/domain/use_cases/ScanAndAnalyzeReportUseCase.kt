package com.example.cliniclearai.domain.use_cases

import android.content.Context
import android.net.Uri
import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.data_sources.local.OcrDataSource
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.di.DefaultDispatcher
import com.example.cliniclearai.domain.models.LabReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Cas d'utilisation : Numérisation d'un bilan papier ou PDF par OCR (Google ML Kit)
 * et analyse structurée avec vulgarisation médicale par l'API Gemini,
 * puis persistance immédiate dans Room (SSOT).
 */
class ScanAndAnalyzeReportUseCase @Inject constructor(
    private val ocrDataSource: OcrDataSource,
    private val medicalRepository: MedicalRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        context: Context,
        documentUri: Uri,
        apiKey: String = "",
        language: String = ""
    ): Resource<LabReport> = withContext(defaultDispatcher) {
        try {
            // 1. Extraction du texte brut via Google ML Kit (images JPG/PNG ou PDF multipages)
            val ocrText = ocrDataSource.recognizeTextFromUri(context, documentUri)
            if (ocrText.isBlank()) {
                return@withContext Resource.Error("Aucun texte lisible n'a été détecté sur le document sélectionné.")
            }

            // 2. Structuration et vulgarisation via Gemini (avec fallback local résilient) + Sauvegarde Room
            medicalRepository.analyzeOcrTextAndSave(ocrText, apiKey, language)
        } catch (e: Exception) {
            Resource.Error(
                message = "Échec du scan : ${e.localizedMessage ?: "Erreur de traitement du document"}",
                cause = e
            )
        }
    }

    suspend fun analyzeTextDirectly(
        ocrText: String,
        apiKey: String = "",
        language: String = ""
    ): Resource<LabReport> = withContext(defaultDispatcher) {
        if (ocrText.isBlank()) {
            return@withContext Resource.Error("Le texte fourni est vide.")
        }
        medicalRepository.analyzeOcrTextAndSave(ocrText, apiKey, language)
    }
}
