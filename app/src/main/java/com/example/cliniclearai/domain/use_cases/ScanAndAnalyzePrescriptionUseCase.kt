package com.example.cliniclearai.domain.use_cases

import android.content.Context
import android.net.Uri
import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.data_sources.local.OcrDataSource
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.di.DefaultDispatcher
import com.example.cliniclearai.domain.models.Drug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Cas d'utilisation : Numérisation d'une ordonnance médicale par OCR (Google ML Kit)
 * et extraction structurée des médicaments prescrits (molécule, posologie, fréquence)
 * via l'API Gemini ou l'analyseur local résilient.
 */
class ScanAndAnalyzePrescriptionUseCase @Inject constructor(
    private val ocrDataSource: OcrDataSource,
    private val medicalRepository: MedicalRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(
        context: Context,
        documentUri: Uri,
        apiKey: String = "",
        language: String = ""
    ): Resource<List<Drug>> = withContext(defaultDispatcher) {
        try {
            val ocrText = ocrDataSource.recognizeTextFromUri(context, documentUri)
            if (ocrText.isBlank()) {
                return@withContext Resource.Error("Aucun texte lisible n'a été détecté sur l'ordonnance.")
            }
            medicalRepository.analyzePrescriptionOcrTextAndSave(ocrText, apiKey, language)
        } catch (e: Exception) {
            Resource.Error(
                message = "Échec du scan de l'ordonnance : ${e.localizedMessage ?: "Erreur de traitement"}",
                cause = e
            )
        }
    }

    suspend fun analyzeTextDirectly(
        ocrText: String,
        apiKey: String = "",
        language: String = ""
    ): Resource<List<Drug>> = withContext(defaultDispatcher) {
        if (ocrText.isBlank()) {
            return@withContext Resource.Error("Le texte de l'ordonnance est vide.")
        }
        medicalRepository.analyzePrescriptionOcrTextAndSave(ocrText, apiKey, language)
    }
}
