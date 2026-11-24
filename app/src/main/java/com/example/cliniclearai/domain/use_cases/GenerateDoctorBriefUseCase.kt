package com.example.cliniclearai.domain.use_cases

import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.di.DefaultDispatcher
import com.example.cliniclearai.domain.models.AlertType
import com.example.cliniclearai.domain.models.DoctorBrief
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Cas d'utilisation : Synthèse clinique multi-sources au format SOAP (Doctor Brief).
 *
 * Points clés d'architecture :
 * - Agrégation multi-sources : Combine de manière réactive les symptômes, bilans et traitements.
 * - Threading : Exécuté sur Dispatchers.Default pour préserver la réactivité du thread UI.
 * - Format SOAP standardisé : Subjectif, Objectif, Analyse (Assessment), Plan d'action.
 * - Stateless : Ne contient aucun état mutable.
 */
class GenerateDoctorBriefUseCase @Inject constructor(
    private val medicalRepository: MedicalRepository,
    private val checkDrugInteractionsUseCase: CheckDrugInteractionsUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): DoctorBrief = withContext(defaultDispatcher) {
        val symptoms = medicalRepository.getSymptoms().first()
        val reports = medicalRepository.getLabReports().first()
        val drugs = medicalRepository.getDrugs().first()
        val drugAlerts = checkDrugInteractionsUseCase.analyze(drugs)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date())

        // Subjective (S) : Symptômes ressentis et décrits par le patient
        val subjective = if (symptoms.isEmpty()) {
            "Aucun symptôme récent consigné par le patient."
        } else {
            val symptomsList = symptoms.sortedByDescending { it.timestamp }.joinToString("\n") { s ->
                "• [${s.bodyPart}] Intensité: ${s.intensity}/10 — ${s.description}" +
                        if (s.associatedFactors.isNotBlank()) " (Facteurs déclenchants: ${s.associatedFactors})" else ""
            }
            "Symptômes rapportés par le patient (${symptoms.size}) :\n$symptomsList"
        }

        // Objective (O) : Données biologiques vérifiées et traitements
        val abnormalBiomarkers = reports.flatMap { r ->
            r.biomarkers.filter { it.isAbnormal }.map {
                val arrow = if (it.value > it.maxReference) "▲ ÉLEVÉ" else "▼ BAS"
                "${it.name}: ${it.value} ${it.unit} [$arrow] (Norme: ${it.minReference}-${it.maxReference} ${it.unit}) [${r.title}]"
            }
        }
        val drugList = drugs.map { d ->
            val prescr = if (d.isPrescription) "Sur ordonnance" else "Automédication"
            "• ${d.brandName} (${d.genericName}) — $prescr | Posologie: ${d.dosage.ifBlank { "N/A" }}, Fréquence: ${d.frequency.ifBlank { "N/A" }}"
        }

        val objective = buildString {
            append("Résultats biologiques et traitements actuels :\n")
            if (abnormalBiomarkers.isNotEmpty()) {
                append("Biomarqueurs hors normes (${abnormalBiomarkers.size}) :\n")
                abnormalBiomarkers.forEach { append("• $it\n") }
            } else {
                append("• Aucun biomarqueur hors norme relevé sur les derniers bilans.\n")
            }
            append("\nTraitements médicamenteux en cours (${drugList.size}) :\n")
            if (drugList.isNotEmpty()) {
                drugList.forEach { append("$it\n") }
            } else {
                append("• Aucun traitement enregistré.\n")
            }
        }.trim()

        // Assessment (A) : Analyse de synthèse et détection d'alertes
        val assessment = buildString {
            append("Analyse de synthèse clinique :\n")
            val recalls = drugs.filter { it.hasActiveRecall }
            if (recalls.isNotEmpty()) {
                append("⚠️ ALERTE SANITAIRE : ${recalls.size} médicament(s) sous rappel actif (${recalls.joinToString { it.brandName }}).\n")
            }
            if (drugAlerts.isNotEmpty()) {
                drugAlerts.forEach { alert ->
                    val prefix = when (alert.type) {
                        AlertType.ACTIVE_RECALL -> "⚠️ ALERTE SANITAIRE"
                        AlertType.DUPLICATE_INGREDIENT -> "⚠️ ATTENTION SURDOSAGE"
                        AlertType.DRUG_INTERACTION -> "⚠️ INTERACTION MÉDICAMENTEUSE"
                    }
                    append("$prefix : ${alert.title}\n${alert.description}\n")
                }
            }
            if (abnormalBiomarkers.isNotEmpty()) {
                append("• Anomalies biologiques constatées nécessitant une surveillance clinique.\n")
            }
            if (symptoms.any { it.intensity >= 7 }) {
                append("• Présence de douleurs intenses (score >= 7/10) signalées par le patient.\n")
            }
            if (recalls.isEmpty() && drugAlerts.isEmpty() && abnormalBiomarkers.isEmpty() && symptoms.none { it.intensity >= 7 }) {
                append("• Tableau clinique globalement stable selon les données renseignées.\n")
            }
        }.trim()

        // Plan (P) : Questions préparées pour la consultation
        val plan = buildString {
            append("Points d'échange et questions préparées pour la consultation :\n")
            var questionIndex = 1
            if (abnormalBiomarkers.isNotEmpty()) {
                append("${questionIndex++}. Faut-il programmer un bilan de contrôle ou un examen complémentaire pour les biomarqueurs atypiques ?\n")
            }
            if (drugAlerts.isNotEmpty()) {
                append("${questionIndex++}. Pouvons-nous réévaluer mes associations médicamenteuses pour écarter les risques d'interactions signalés ?\n")
            }
            if (symptoms.isNotEmpty()) {
                append("${questionIndex++}. Quelle est l'origine probable des symptômes physiques déclarés récemment ?\n")
            }
            append("${questionIndex++}. Y a-t-il des adaptations hygiéno-diététiques ou préventives recommandées d'ici la prochaine consultation ?")
        }.trim()

        DoctorBrief(
            id = UUID.randomUUID().toString(),
            generatedDate = dateStr,
            subjective = subjective,
            objective = objective,
            assessment = assessment,
            plan = plan
        )
    }
}
