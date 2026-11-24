package com.example.cliniclearai.data.data_sources.remote.dto

import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.BiomarkerStatus
import com.example.cliniclearai.domain.models.LabReport
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Requête pour l'API Gemini
data class GeminiRequestDto(
    @SerializedName("contents") val contents: List<GeminiContentDto>,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfigDto? = GeminiGenerationConfigDto()
)

data class GeminiContentDto(
    @SerializedName("parts") val parts: List<GeminiPartDto>
)

data class GeminiPartDto(
    @SerializedName("text") val text: String
)

data class GeminiGenerationConfigDto(
    @SerializedName("temperature") val temperature: Double = 0.2,
    @SerializedName("responseMimeType") val responseMimeType: String = "application/json"
)

// Réponse de l'API Gemini
data class GeminiResponseDto(
    @SerializedName("candidates") val candidates: List<GeminiCandidateDto>? = null
)

data class GeminiCandidateDto(
    @SerializedName("content") val content: GeminiContentDto? = null
)

// Schéma JSON structuré attendu de Gemini pour un bilan biologique
data class AiReportDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("laboratory") val laboratory: String? = null,
    @SerializedName("doctorSummary") val doctorSummary: String? = null,
    @SerializedName("biomarkers") val biomarkers: List<AiBiomarkerDto>? = null
)

data class AiBiomarkerDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("minReference") val minReference: Double? = null,
    @SerializedName("maxReference") val maxReference: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("vulgarization") val vulgarization: String? = null,
    @SerializedName("recommendation") val recommendation: String? = null
)

fun AiReportDto.toDomain(): LabReport {
    val reportDate = date?.ifBlank { null }
        ?: SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())

    val domainBiomarkers = biomarkers?.mapIndexed { index, b ->
        val minRef = b.minReference ?: 0.0
        val maxRef = b.maxReference ?: 0.0
        val valDouble = b.value ?: 0.0

        val computedStatus = when (b.status?.uppercase()) {
            "CRITICAL" -> BiomarkerStatus.CRITICAL
            "HIGH" -> BiomarkerStatus.HIGH
            "LOW" -> BiomarkerStatus.LOW
            "NORMAL" -> BiomarkerStatus.NORMAL
            else -> {
                if (maxRef > 0 && valDouble > maxRef) BiomarkerStatus.HIGH
                else if (minRef > 0 && valDouble < minRef) BiomarkerStatus.LOW
                else BiomarkerStatus.NORMAL
            }
        }

        Biomarker(
            id = "bio_ai_${index}_${UUID.randomUUID().toString().take(6)}",
            name = b.name ?: "Biomarqueur inconnu",
            value = valDouble,
            unit = b.unit ?: "",
            minReference = minRef,
            maxReference = maxRef,
            status = computedStatus,
            vulgarization = b.vulgarization ?: "Mesure biologique analysée par IA.",
            recommendation = b.recommendation ?: "À interpréter avec votre médecin traitant."
        )
    } ?: emptyList()

    return LabReport(
        id = "report_ai_${UUID.randomUUID()}",
        title = title?.ifBlank { null } ?: "Bilan d'Analyses Biologiques",
        date = reportDate,
        laboratory = laboratory?.ifBlank { null } ?: "Laboratoire d'Analyses Médicales",
        biomarkers = domainBiomarkers,
        doctorSummary = doctorSummary?.ifBlank { null } ?: "Analyse et structuration automatisées par CliniClear AI.",
        hasAlerts = domainBiomarkers.any { it.isAbnormal }
    )
}
