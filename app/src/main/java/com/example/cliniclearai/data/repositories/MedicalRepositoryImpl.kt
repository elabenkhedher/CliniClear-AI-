package com.example.cliniclearai.data.repositories

import android.util.Log
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.data_sources.local.BiomarkerLocalizationHelper
import com.example.cliniclearai.data.data_sources.local.CliniClearDatabase
import com.example.cliniclearai.data.data_sources.local.MedicalDao
import com.example.cliniclearai.data.data_sources.local.OcrFallbackParser
import com.example.cliniclearai.data.data_sources.local.PrescriptionFallbackParser
import com.example.cliniclearai.data.data_sources.local.UserPreferencesDataSource
import com.example.cliniclearai.data.data_sources.local.entities.toDomain
import com.example.cliniclearai.data.data_sources.local.entities.toEntity
import com.example.cliniclearai.data.data_sources.remote.AiAnalysisApi
import com.example.cliniclearai.data.data_sources.remote.OpenFdaApi
import com.example.cliniclearai.data.data_sources.remote.dto.AiReportDto
import com.example.cliniclearai.data.data_sources.remote.dto.PrescriptionResponseDto
import com.example.cliniclearai.data.data_sources.remote.dto.GeminiContentDto
import com.example.cliniclearai.data.data_sources.remote.dto.GeminiPartDto
import com.example.cliniclearai.data.data_sources.remote.dto.GeminiRequestDto
import com.example.cliniclearai.data.data_sources.remote.dto.toDomain
import com.example.cliniclearai.di.IoDispatcher
import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.DrugRecall
import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.domain.models.SymptomEntry
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicalRepositoryImpl @Inject constructor(
    private val medicalDao: MedicalDao,
    private val openFdaApi: OpenFdaApi,
    private val aiAnalysisApi: AiAnalysisApi,
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val gson: Gson,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MedicalRepository {

    // Thread-safe in-memory cache for fast report detail retrieval
    private val cacheMutex = Mutex()
    private val reportDetailCache = mutableMapOf<String, LabReport>()

    override suspend fun initializeSeedDataIfEmpty(): Unit = withContext(ioDispatcher) {
        // Nettoyage systématique des données de test injectées par défaut
        clearTestData()
    }

    override suspend fun clearTestData(): Unit = withContext(ioDispatcher) {
        medicalDao.deleteLabReportById("report_annual_2026")
        medicalDao.deleteLabReportById("report_lipid_2026")
        medicalDao.deleteDrugById("drug_doliprane_1")
        medicalDao.deleteDrugById("drug_lipitor_2")
        cacheMutex.withLock {
            reportDetailCache.remove("report_annual_2026")
            reportDetailCache.remove("report_lipid_2026")
            reportDetailCache.remove("drug_doliprane_1")
            reportDetailCache.remove("drug_lipitor_2")
        }
    }

    override suspend fun clearAllData(): Unit = withContext(ioDispatcher) {
        medicalDao.deleteAllLabReports()
        medicalDao.deleteAllDrugs()
        medicalDao.deleteAllSymptoms()
        cacheMutex.withLock {
            reportDetailCache.clear()
        }
    }

    // --- Lab Reports (SSOT: Room via Flow) ---

    override fun getLabReports(): Flow<List<LabReport>> {
        return medicalDao.getAllLabReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLabReportById(id: String): Flow<LabReport?> {
        return medicalDao.getLabReportById(id).map { it?.toDomain() }
    }

    override suspend fun getLabReportDetail(id: String): Resource<LabReport> = withContext(ioDispatcher) {
        // 1. Check thread-safe memory cache first
        cacheMutex.withLock {
            reportDetailCache[id]?.let { cachedReport ->
                return@withContext Resource.Success(cachedReport)
            }
        }

        // 2. Fallback to Room Database (Single Source of Truth)
        val entity = medicalDao.getLabReportByIdDirect(id)
        if (entity != null) {
            val domainReport = entity.toDomain()
            // Update cache
            cacheMutex.withLock {
                reportDetailCache[id] = domainReport
            }
            Resource.Success(domainReport)
        } else {
            Resource.Error("Bilan médical introuvable (id: $id)")
        }
    }

    override suspend fun saveLabReport(report: LabReport): Unit = withContext(ioDispatcher) {
        medicalDao.insertLabReport(report.toEntity())
        cacheMutex.withLock {
            reportDetailCache[report.id] = report
        }
    }

    override suspend fun deleteLabReport(id: String): Unit = withContext(ioDispatcher) {
        medicalDao.deleteLabReportById(id)
        cacheMutex.withLock {
            reportDetailCache.remove(id)
        }
    }

    override suspend fun analyzeOcrTextAndSave(
        ocrText: String,
        apiKey: String,
        language: String
    ): Resource<LabReport> = withContext(ioDispatcher) {
        val targetLang = language.ifBlank {
            try {
                userPreferencesDataSource.userPreferencesFlow.firstOrNull()?.explanationLanguage ?: "fr"
            } catch (e: Exception) {
                "fr"
            }
        }.lowercase().take(2).ifBlank { "fr" }

        Log.d("CliniClearOCR", "Texte OCR extrait à analyser :\n$ocrText")

        try {
            val keyToUse = apiKey.ifBlank {
                try {
                    val customKey = userPreferencesDataSource.userPreferencesFlow.firstOrNull()?.geminiApiKey ?: ""
                    customKey.ifBlank { Constants.DEFAULT_GEMINI_API_KEY }
                } catch (e: Exception) {
                    Constants.DEFAULT_GEMINI_API_KEY
                }
            }
            if (keyToUse.isNotBlank()) {
                val languageInstruction = when (targetLang) {
                    "en" -> """
                        CRITICAL INSTRUCTION ON LANGUAGE:
                        The patient requires explanations and analysis in ENGLISH.
                        You MUST formulate the following fields STRICTLY IN ENGLISH:
                        - "title": Report title in English (e.g. "Comprehensive Blood Test")
                        - "doctorSummary": Overall medical synthesis for the patient in English
                        - "name": Clean name of the biomarker in English (e.g. "Hemoglobin", "Total Cholesterol", "Creatinine")
                        - "vulgarization": Clear, simple, reassuring explanation of its physiological role in English
                        - "recommendation": Practical dietary/lifestyle or medical advice in English
                    """.trimIndent()
                    "ar" -> """
                        CRITICAL INSTRUCTION ON LANGUAGE:
                        The patient requires explanations and analysis in ARABIC (العربية).
                        You MUST formulate the following fields STRICTLY IN ARABIC:
                        - "title": عنوان التقرير باللغة العربية (مثال: "تحليل دم شامل")
                        - "doctorSummary": ملخص طبي شامل للمريض باللغة العربية
                        - "name": اسم المؤشر باللغة العربية (مثال: "الهيموغلوبين", "الكوليسترول الكلي", "الكرياتينين")
                        - "vulgarization": شرح مبسط ومطمئن لدور المؤشر في الجسم باللغة العربية
                        - "recommendation": نصيحة غذائية أو طبية عملية باللغة العربية
                    """.trimIndent()
                    else -> """
                        Rédige impérativement les champs suivants en FRANÇAIS :
                        - "title": Titre du bilan en français (ex: "Bilan Biologique Complet")
                        - "doctorSummary": Synthèse médicale globale récapitulant les principaux points en français
                        - "name": Nom clair et complet du biomarqueur en français
                        - "vulgarization": Explication simple et rassurante de son rôle pour le patient en français
                        - "recommendation": Conseil hygiéno-diététique ou avis médical adapté en français
                    """.trimIndent()
                }

                val prompt = """
                    Tu es un médecin biologiste expert. Analyse l'intégralité du texte suivant issu de la numérisation OCR d'un bilan d'analyses médicales (pouvant comporter plusieurs pages) :
                    '''
                    $ocrText
                    '''
                    
                    $languageInstruction

                    Instructions impératives :
                    1. Extrais TOUS les biomarqueurs mesurés dans le document sans exception (Hématologie, Numération Formule Sanguine NFS, Biochimie, Ionogramme, Bilan Rénal, Bilan Hépatique, Bilan Lipidique, Hormonologie/Thyroïde, Vitamines, Marqueurs inflammatoires, etc.).
                    2. Le texte OCR est reconstruit ligne par ligne avec des colonnes séparées par le caractère '|'. Lis chaque ligne horizontalement de gauche à droite sans mélanger les colonnes entre différentes lignes ou différents biomarqueurs.
                    3. Convertis impérativement les virgules décimales en points (ex : 4,44 → 4.44, 0,70 → 0.70). Le champ "value" doit impérativement être un nombre flottant JSON valide et non une chaîne de caractères.
                    4. Veille à ne jamais attribuer la valeur d'une autre ligne ou colonne à un biomarqueur (ex : si la Ferritine vaut 4.44, n'attribue pas le nombre 76000 d'une ligne voisine).
                    5. Pour chaque biomarqueur extrait :
                       - "name": Nom clair du biomarqueur dans la langue demandée
                       - "value": Valeur numérique mesurée (ex: 4.44)
                       - "unit": Unité exacte du laboratoire (ex: ng/mL, g/dL, µmol/L, UI/L, %, mmol/L...)
                       - "minReference": Borne minimale de référence indiquée par le laboratoire (ou 0.0 si non applicable)
                       - "maxReference": Borne maximale de référence indiquée par le laboratoire
                       - "status": NORMAL, LOW (inférieur), HIGH (supérieur), ou CRITICAL (très hors norme)
                       - "vulgarization": Explication simple et rassurante dans la langue demandée
                       - "recommendation": Conseil hygiéno-diététique ou avis médical adapté dans la langue demandée
                    6. Extrais le nom réel du laboratoire, la date d'analyse et rédige une synthèse médicale globale ("doctorSummary") dans la langue demandée.

                    Renvoie STRICTEMENT un objet JSON valide au format suivant :
                    {
                      "title": "Titre du bilan",
                      "date": "YYYY-MM-DD",
                      "laboratory": "Nom du laboratoire",
                      "doctorSummary": "Synthèse globale récapitulant les principaux points d'attention",
                      "biomarkers": [
                        {
                          "name": "Nom du biomarqueur",
                          "value": 14.5,
                          "unit": "g/dL",
                          "minReference": 13.0,
                          "maxReference": 17.0,
                          "status": "NORMAL",
                          "vulgarization": "Explication claire",
                          "recommendation": "Conseil pratique"
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequestDto(
                    contents = listOf(
                        GeminiContentDto(
                            parts = listOf(GeminiPartDto(text = prompt))
                        )
                    )
                )

                Log.d("CliniClearAI", "Envoi de la requête d'analyse médicale à Gemini API...")
                val response = aiAnalysisApi.generateContent(
                    apiKey = keyToUse,
                    request = request
                )

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Log.d("CliniClearAI", "Réponse Gemini brute reçue :\n$rawText")

                if (!rawText.isNullOrBlank()) {
                    val firstBrace = rawText.indexOf('{')
                    val lastBrace = rawText.lastIndexOf('}')
                    if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                        val cleanJson = rawText.substring(firstBrace, lastBrace + 1).trim()
                        val aiReportDto = gson.fromJson(cleanJson, AiReportDto::class.java)
                        val domainReport = aiReportDto.toDomain()
                        saveLabReport(domainReport)
                        Log.d("CliniClearAI", "Rapport IA analysé et sauvegardé avec succès (${domainReport.biomarkers.size} biomarqueurs)")
                        return@withContext Resource.Success(domainReport)
                    } else {
                        Log.e("CliniClearAI", "Format JSON introuvable dans la réponse Gemini")
                    }
                }
            }

            // Fallback intelligent si clé absente ou réponse vide
            Log.d("CliniClearAI", "Exécution du fallback local OcrFallbackParser...")
            val fallbackReport = OcrFallbackParser.parse(ocrText, language = targetLang)
            saveLabReport(fallbackReport)
            Resource.Success(fallbackReport)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("CliniClearAI", "Erreur lors de l'analyse Gemini : ${e.message}", e)
            val fallbackReport = OcrFallbackParser.parse(ocrText, language = targetLang)
            saveLabReport(fallbackReport)
            Resource.Success(fallbackReport)
        }
    }

    override suspend fun updateReportLanguage(
        reportId: String,
        language: String
    ): Resource<LabReport> = withContext(ioDispatcher) {
        val existingEntity = medicalDao.getLabReportByIdDirect(reportId)
            ?: return@withContext Resource.Error("Bilan médical introuvable (id: $reportId)")

        val existingReport = existingEntity.toDomain()
        val targetLang = language.lowercase().take(2).ifBlank { "fr" }
        val localizedReport = BiomarkerLocalizationHelper.localizeReport(existingReport, targetLang)
        saveLabReport(localizedReport)
        Resource.Success(localizedReport)
    }

    // --- Drugs ---

    override fun getDrugs(): Flow<List<Drug>> {
        return medicalDao.getAllDrugs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDrugById(id: String): Flow<Drug?> {
        return medicalDao.getDrugById(id).map { it?.toDomain() }
    }

    override suspend fun saveDrug(drug: Drug): Unit = withContext(ioDispatcher) {
        medicalDao.insertDrug(drug.toEntity())
    }

    override suspend fun deleteDrug(id: String): Unit = withContext(ioDispatcher) {
        medicalDao.deleteDrugById(id)
    }

    override suspend fun updateDrugRecallStatus(drugId: String, hasRecall: Boolean): Unit = withContext(ioDispatcher) {
        medicalDao.updateDrugRecallStatus(drugId, hasRecall)
    }

    override suspend fun analyzePrescriptionOcrTextAndSave(
        ocrText: String,
        apiKey: String,
        language: String
    ): Resource<List<Drug>> = withContext(ioDispatcher) {
        val targetLang = language.ifBlank {
            try {
                userPreferencesDataSource.userPreferencesFlow.firstOrNull()?.explanationLanguage ?: "fr"
            } catch (e: Exception) {
                "fr"
            }
        }.lowercase().take(2).ifBlank { "fr" }
        try {
            val keyToUse = apiKey.ifBlank {
                try {
                    val customKey = userPreferencesDataSource.userPreferencesFlow.firstOrNull()?.geminiApiKey ?: ""
                    customKey.ifBlank { Constants.DEFAULT_GEMINI_API_KEY }
                } catch (e: Exception) {
                    Constants.DEFAULT_GEMINI_API_KEY
                }
            }
            if (keyToUse.isNotBlank()) {
                val prompt = """
                    Tu es un pharmacien clinicien expert. Analyse le texte suivant issu de la numérisation OCR d'une ordonnance médicale :
                    '''
                    $ocrText
                    '''
                    Instructions impératives :
                    1. Extrais TOUS les médicaments prescrits dans l'ordonnance.
                    2. Pour chaque médicament, renseigne précisément :
                       - "brandName": Nom commercial lisible (ex: Kardegic, Advil, Doliprane, Metformine, Tahor...)
                       - "genericName": DCI ou dénomination commune (ex: Acide acétylsalicylique, Ibuprofène, Paracétamol...)
                       - "activeIngredients": Tableau des substances actives en minuscules (ex: ["acide acetylsalicylique"] ou ["paracetamol"])
                       - "dosage": Dosage prescrit (ex: 75 mg, 400 mg, 1000 mg...)
                       - "frequency": Posologie / fréquence de prise (ex: 1 sachet par jour au repas, 3 fois par jour si douleur...)
                       - "instructions": Modalités de prise et conseils particuliers
                       - "purpose": Indication thérapeutique
                       - "warnings": Mises en garde et précautions essentielles
                       - "isPrescription": true

                    Renvoie STRICTEMENT un objet JSON valide avec cette structure :
                    {
                      "doctorName": "Nom du médecin si détecté",
                      "prescriptionDate": "YYYY-MM-DD",
                      "drugs": [
                        {
                          "brandName": "Nom du médicament",
                          "genericName": "DCI",
                          "activeIngredients": ["substance"],
                          "dosage": "100 mg",
                          "frequency": "1 cp/jour",
                          "instructions": "Au cours du repas",
                          "purpose": "Indication",
                          "warnings": "Précautions",
                          "isPrescription": true
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequestDto(
                    contents = listOf(
                        GeminiContentDto(
                            parts = listOf(GeminiPartDto(text = prompt))
                        )
                    )
                )

                val response = aiAnalysisApi.generateContent(
                    apiKey = keyToUse,
                    request = request
                )

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val cleanJson = rawText
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val objStart = cleanJson.indexOf('{')
                    val objEnd = cleanJson.lastIndexOf('}')
                    val arrStart = cleanJson.indexOf('[')
                    val arrEnd = cleanJson.lastIndexOf(']')

                    val parsedDrugs = try {
                        if (arrStart != -1 && arrEnd > arrStart && (objStart == -1 || arrStart < objStart)) {
                            val arrString = cleanJson.substring(arrStart, arrEnd + 1)
                            val listType = object : com.google.gson.reflect.TypeToken<List<com.example.cliniclearai.data.data_sources.remote.dto.PrescriptionDrugDto>>() {}.type
                            val list: List<com.example.cliniclearai.data.data_sources.remote.dto.PrescriptionDrugDto> = gson.fromJson(arrString, listType)
                            list.map { it.toDomain() }
                        } else if (objStart != -1 && objEnd > objStart) {
                            val objString = cleanJson.substring(objStart, objEnd + 1)
                            val prescriptionDto = gson.fromJson(objString, PrescriptionResponseDto::class.java)
                            prescriptionDto.drugs?.map { it.toDomain() } ?: emptyList()
                        } else {
                            emptyList()
                        }
                    } catch (_: Exception) {
                        emptyList()
                    }

                    if (parsedDrugs.isNotEmpty()) {
                        medicalDao.insertDrugs(parsedDrugs.map { it.toEntity() })
                        return@withContext Resource.Success(parsedDrugs)
                    }
                }
            }

            // Fallback intelligent local (Dictionnaire 130+ médicaments + extracteur heuristique)
            val fallbackDrugs = PrescriptionFallbackParser.parse(ocrText)
            if (fallbackDrugs.isNotEmpty()) {
                medicalDao.insertDrugs(fallbackDrugs.map { it.toEntity() })
                Resource.Success(fallbackDrugs)
            } else {
                Resource.Error("Aucun médicament lisible n'a été détecté. Assurez-vous que l'ordonnance est bien nette et éclairée.")
            }
        } catch (e: Exception) {
            val fallbackDrugs = PrescriptionFallbackParser.parse(ocrText)
            if (fallbackDrugs.isNotEmpty()) {
                medicalDao.insertDrugs(fallbackDrugs.map { it.toEntity() })
                Resource.Success(fallbackDrugs)
            } else {
                Resource.Error("Impossible de traiter l'ordonnance : ${e.localizedMessage ?: "Erreur d'analyse"}")
            }
        }
    }

    // --- Remote OpenFDA ---

    override suspend fun searchDrugsOnline(query: String): Resource<List<Drug>> = withContext(ioDispatcher) {
        try {
            val response = openFdaApi.searchDrugLabels(
                searchQuery = "openfda.brand_name:\"$query\""
            )
            val results = response.results?.map { it.toDomain() } ?: emptyList()
            Resource.Success(results)
        } catch (e: Exception) {
            Resource.Error(
                message = "Erreur de connexion à l'API OpenFDA : ${e.localizedMessage ?: "Erreur réseau"}",
                cause = e
            )
        }
    }

    override suspend fun checkDrugRecallsOnline(): Resource<List<DrugRecall>> = withContext(ioDispatcher) {
        try {
            val response = openFdaApi.getDrugRecalls(searchQuery = "status:Ongoing")
            val recalls = response.results?.map { it.toDomain() } ?: emptyList()

            // Compare with local drugs and update Room SSOT
            val localDrugs = medicalDao.getAllDrugs().firstOrNull() ?: emptyList()
            for (drug in localDrugs) {
                val matchesRecall = recalls.any { recall ->
                    recall.productDescription.contains(drug.brandName, ignoreCase = true) ||
                            recall.productDescription.contains(drug.genericName, ignoreCase = true) ||
                            (recall.brandName.isNotBlank() && recall.brandName.contains(drug.brandName, ignoreCase = true))
                }
                if (matchesRecall != drug.hasActiveRecall) {
                    medicalDao.updateDrugRecallStatus(drug.id, matchesRecall)
                }
            }

            Resource.Success(recalls)
        } catch (e: Exception) {
            Resource.Error(
                message = "Impossible de vérifier les rappels OpenFDA : ${e.localizedMessage ?: "Erreur réseau"}",
                cause = e
            )
        }
    }

    // --- Symptoms ---

    override fun getSymptoms(): Flow<List<SymptomEntry>> {
        return medicalDao.getAllSymptoms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSymptom(symptom: SymptomEntry): Unit = withContext(ioDispatcher) {
        medicalDao.insertSymptom(symptom.toEntity())
    }

    override suspend fun deleteSymptom(id: String): Unit = withContext(ioDispatcher) {
        medicalDao.deleteSymptomById(id)
    }
}
