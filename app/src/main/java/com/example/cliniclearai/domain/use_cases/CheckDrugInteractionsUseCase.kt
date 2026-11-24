package com.example.cliniclearai.domain.use_cases

import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.di.DefaultDispatcher
import com.example.cliniclearai.domain.models.Drug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Cas d'utilisation : Détection des interactions médicamenteuses et alertes sanitaires.
 *
 * Points clés d'architecture :
 * - Threading & Dispatchers : Les algorithmes d'
 *
 * analyse et de croisement d'ingrédients actifs
 *   sont exécutés hors du thread UI sur Dispatchers.Default (DefaultDispatcher).
 * - SSOT : La liste des médicaments provient en continu du flux réactif Room.
 * - Stateless : Ne stocke aucun état mutable.
 */
class CheckDrugInteractionsUseCase @Inject constructor(
    private val medicalRepository: MedicalRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<List<DrugAlert>> {
        return medicalRepository.getDrugs().map { drugs ->
            withContext(defaultDispatcher) {
                analyze(drugs)
            }
        }.flowOn(defaultDispatcher)
    }

    suspend fun analyze(drugs: List<Drug>): List<DrugAlert> = withContext(defaultDispatcher) {
        if (drugs.isEmpty()) {
            return@withContext emptyList()
        }

        val alerts = mutableListOf<DrugAlert>()

        // 1. Alertes de rappel sanitaire actif (OpenFDA Enforcement)
        drugs.filter { it.hasActiveRecall }.forEach { drug ->
            alerts.add(
                DrugAlert(
                    type = AlertType.ACTIVE_RECALL,
                    title = "Rappel sanitaire actif : ${drug.brandName}",
                    description = "Ce médicament fait l'objet d'un rappel actif enregistré par l'agence sanitaire (OpenFDA). Consultez immédiatement votre pharmacien ou médecin.",
                    involvedDrugs = listOf(drug.brandName)
                )
            )
        }

        // 2. Détection de doublons d'ingrédients actifs (Risque de surdosage)
        val ingredientMap = mutableMapOf<String, MutableList<String>>()
        drugs.forEach { drug ->
            val ingredients = if (drug.activeIngredients.isNotEmpty()) {
                drug.activeIngredients
            } else {
                listOf(drug.genericName)
            }

            for (ingredient in ingredients) {
                val normalized = ingredient.trim().lowercase()
                if (normalized.isNotBlank()) {
                    ingredientMap.getOrPut(normalized) { mutableListOf() }.add(drug.brandName)
                }
            }
        }

        ingredientMap.filter { it.value.distinct().size > 1 }.forEach { (ingredient, drugList) ->
            val distinctDrugs = drugList.distinct()
            alerts.add(
                DrugAlert(
                    type = AlertType.DUPLICATE_INGREDIENT,
                    title = "Risque de surdosage : Ingrédient actif en doublon",
                    description = "L'ingrédient actif \"${ingredient.replaceFirstChar { it.uppercase() }}\" est présent simultanément dans ${distinctDrugs.joinToString(", ")}. Risque de toxicité accrue.",
                    involvedDrugs = distinctDrugs
                )
            )
        }

        // 3. Détection d'interactions médicamenteuses croisées
        for (i in 0 until drugs.size) {
            for (j in i + 1 until drugs.size) {
                val drugA = drugs[i]
                val drugB = drugs[j]
                val textA = "${drugA.brandName} ${drugA.genericName} ${drugA.activeIngredients.joinToString(" ")}".lowercase()
                val textB = "${drugB.brandName} ${drugB.genericName} ${drugB.activeIngredients.joinToString(" ")}".lowercase()

                checkPairInteraction(drugA, textA, drugB, textB)?.let { alert ->
                    alerts.add(alert)
                }
            }
        }

        alerts
    }

    private fun checkPairInteraction(
        drugA: Drug,
        textA: String,
        drugB: Drug,
        textB: String
    ): DrugAlert? {
        val anticoagulants = listOf("aspirin", "acide acetylsalicylique", "clopidogrel", "warfarin", "apixaban", "rivaroxaban", "kardegic", "eliquis", "xarelto", "previscan")
        val nsaids = listOf("ibuprofen", "ketoprofen", "naproxen", "diclofenac", "advil", "nurofen", "voltarène", "antarene")
        val statins = listOf("atorvastatin", "simvastatin", "rosuvastatin", "pravastatin", "lipitor", "tahor", "crestor", "zocor")
        val fibrates = listOf("fenofibrate", "gemfibrozil", "lipanthyl")
        val aceOrArb = listOf("ramipril", "enalapril", "lisinopril", "losartan", "valsartan", "triatec", "cozaar")
        val potassiumDrugs = listOf("spironolactone", "eplerenone", "aldactone", "diffu-k", "kaleorid")
        val sedatives = listOf("diazepam", "lorazepam", "alprazolam", "bromazepam", "valium", "temesta", "xanax", "lexomil")
        val opioids = listOf("tramadol", "codeine", "morphine", "oxycodone", "topalgic", "ixprim", "dafalgan codeine")

        // Anticoagulant + AINS
        if ((anticoagulants.any { textA.contains(it) } && nsaids.any { textB.contains(it) }) ||
            (anticoagulants.any { textB.contains(it) } && nsaids.any { textA.contains(it) })) {
            return DrugAlert(
                type = AlertType.DRUG_INTERACTION,
                title = "Interaction majeure : Anticoagulant/Antiagrégant + AINS",
                description = "L'association de ${drugA.brandName} et ${drugB.brandName} majore considérablement le risque d'hémorragie digestive et interne.",
                involvedDrugs = listOf(drugA.brandName, drugB.brandName)
            )
        }

        // Statine + Fibrate
        if ((statins.any { textA.contains(it) } && fibrates.any { textB.contains(it) }) ||
            (statins.any { textB.contains(it) } && fibrates.any { textA.contains(it) })) {
            return DrugAlert(
                type = AlertType.DRUG_INTERACTION,
                title = "Interaction élevée : Statine + Fibrate",
                description = "L'association de ${drugA.brandName} et ${drugB.brandName} multiplie le risque d'atteinte musculaire et de rhabdomyolyse.",
                involvedDrugs = listOf(drugA.brandName, drugB.brandName)
            )
        }

        // IEC/Sartans + Épargneurs de potassium
        if ((aceOrArb.any { textA.contains(it) } && potassiumDrugs.any { textB.contains(it) }) ||
            (aceOrArb.any { textB.contains(it) } && potassiumDrugs.any { textA.contains(it) })) {
            return DrugAlert(
                type = AlertType.DRUG_INTERACTION,
                title = "Interaction élevée : Risque d'hyperkaliémie",
                description = "La prise conjointe de ${drugA.brandName} et ${drugB.brandName} peut entraîner une élévation anormale du taux de potassium sanguin.",
                involvedDrugs = listOf(drugA.brandName, drugB.brandName)
            )
        }

        // Sédatifs + Opioïdes
        if ((sedatives.any { textA.contains(it) } && opioids.any { textB.contains(it) }) ||
            (sedatives.any { textB.contains(it) } && opioids.any { textA.contains(it) })) {
            return DrugAlert(
                type = AlertType.DRUG_INTERACTION,
                title = "Interaction majeure : Sédatifs + Opioïdes",
                description = "L'association de ${drugA.brandName} et ${drugB.brandName} majore le risque de sédation profonde et de dépression respiratoire.",
                involvedDrugs = listOf(drugA.brandName, drugB.brandName)
            )
        }

        return null
    }
}
