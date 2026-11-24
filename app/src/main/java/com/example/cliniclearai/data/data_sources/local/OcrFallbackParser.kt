package com.example.cliniclearai.data.data_sources.local

import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.BiomarkerStatus
import com.example.cliniclearai.domain.models.LabReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Analyseur clinique avancé local pour extraire tous les biomarqueurs
 * d'un bilan sanguin multipages (NFS, Biochimie, Ionogramme, Foie, Reins, Thyroïde, etc.)
 * issu de l'OCR de photos ou de documents PDF, même hors-ligne.
 */
object OcrFallbackParser {

    data class BiomarkerDefinition(
        val standardName: String,
        val keywords: List<String>,
        val defaultUnit: String,
        val defaultMinRef: Double,
        val defaultMaxRef: Double,
        val vulgarization: String,
        val recommendation: String
    )

    // Base de données médicale couvrant les analyses biologiques courantes
    private val medicalDictionary = listOf(
        // --- HÉMATOLOGIE & NUMÉRATION FORMULE SANGUINE (NFS) ---
        BiomarkerDefinition(
            standardName = "Hémoglobine",
            keywords = listOf("hémoglobine", "hemoglobine", "hb"),
            defaultUnit = "g/dL",
            defaultMinRef = 12.0,
            defaultMaxRef = 17.0,
            vulgarization = "Protéine des globules rouges chargée de transporter l'oxygène dans tout le corps.",
            recommendation = "Consommer des aliments riches en fer et vitamine C pour favoriser l'oxygénation tissulaire."
        ),
        BiomarkerDefinition(
            standardName = "Hématies (Globules Rouges)",
            keywords = listOf("hématies", "hematies", "globules rouges", "erythrocytes", "rbc"),
            defaultUnit = "10^6/µL",
            defaultMinRef = 4.0,
            defaultMaxRef = 5.7,
            vulgarization = "Cellules sanguines transportant l'oxygène depuis les poumons vers tous les organes.",
            recommendation = "À surveiller en cas de fatigue inexpliquée ou de pâleur."
        ),
        BiomarkerDefinition(
            standardName = "Hématocrite",
            keywords = listOf("hématocrite", "hematocrite", "ht"),
            defaultUnit = "%",
            defaultMinRef = 37.0,
            defaultMaxRef = 50.0,
            vulgarization = "Pourcentage du volume sanguin occupé par les globules rouges.",
            recommendation = "Assurer une bonne hydratation quotidienne."
        ),
        BiomarkerDefinition(
            standardName = "VGM (Volume Globulaire Moyen)",
            keywords = listOf("vgm", "volume globulaire moyen", "mcv"),
            defaultUnit = "fL",
            defaultMinRef = 80.0,
            defaultMaxRef = 100.0,
            vulgarization = "Indicateur de la taille moyenne de vos globules rouges (dépistage anémie micro/macrocytaire).",
            recommendation = "Un taux anormal peut orienter vers une carence martiale ou vitaminique (B9/B12)."
        ),
        BiomarkerDefinition(
            standardName = "TCMH",
            keywords = listOf("tcmh", "teneur corpusculaire", "mch"),
            defaultUnit = "pg",
            defaultMinRef = 27.0,
            defaultMaxRef = 33.0,
            vulgarization = "Quantité moyenne d'hémoglobine contenue dans chaque globule rouge.",
            recommendation = "Participe au diagnostic de la qualité des globules rouges."
        ),
        BiomarkerDefinition(
            standardName = "CCMH",
            keywords = listOf("ccmh", "concentration corpusculaire", "mchc"),
            defaultUnit = "g/dL",
            defaultMinRef = 32.0,
            defaultMaxRef = 36.0,
            vulgarization = "Concentration en hémoglobine à l'intérieur des globules rouges.",
            recommendation = "Indicateur de chromie sanguine."
        ),
        BiomarkerDefinition(
            standardName = "Leucocytes (Globules Blancs)",
            keywords = listOf("leucocytes", "globules blancs", "wbc"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 4.0,
            defaultMaxRef = 10.0,
            vulgarization = "Cellules maîtresses de votre système immunitaire protégeant contre infections et inflammations.",
            recommendation = "Une augmentation temporaire reflète souvent une réponse normale à une infection."
        ),
        BiomarkerDefinition(
            standardName = "Polynucléaires Neutrophiles",
            keywords = listOf("neutrophiles", "polynucleaires neutrophiles", "polynucléaires neutrophiles"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 1.5,
            defaultMaxRef = 7.5,
            vulgarization = "Première ligne de défense bactérienne du système immunitaire.",
            recommendation = "Marqueur réactif en cas d'infection bactérienne ou d'inflammation."
        ),
        BiomarkerDefinition(
            standardName = "Lymphocytes",
            keywords = listOf("lymphocytes"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 1.0,
            defaultMaxRef = 4.0,
            vulgarization = "Cellules immunitaires spécialisées dans la lutte contre les virus et la mémoire immunitaire.",
            recommendation = "Un taux élevé accompagne souvent les épisodes viraux récents."
        ),
        BiomarkerDefinition(
            standardName = "Monocytes",
            keywords = listOf("monocytes"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 0.2,
            defaultMaxRef = 1.0,
            vulgarization = "Cellules éboueuses nettoyant les débris cellulaires et combattant les agents pathogènes.",
            recommendation = "S'élèvent lors de la phase de guérison de nombreuses infections."
        ),
        BiomarkerDefinition(
            standardName = "Éosinophiles",
            keywords = listOf("éosinophiles", "eosinophiles"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 0.04,
            defaultMaxRef = 0.5,
            vulgarization = "Cellules impliquées dans les réactions allergiques et la défense antiparasitaire.",
            recommendation = "À surveiller en cas de terrain allergique (asthme, eczéma) ou séjour en zone tropicale."
        ),
        BiomarkerDefinition(
            standardName = "Basophiles",
            keywords = listOf("basophiles"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 0.01,
            defaultMaxRef = 0.1,
            vulgarization = "Cellules participant à la libération d'histamine lors de réactions immunitaires.",
            recommendation = "Taux généralement très faible à l'état normal."
        ),
        BiomarkerDefinition(
            standardName = "Plaquettes",
            keywords = listOf("plaquettes", "thrombocytes", "plt"),
            defaultUnit = "10^3/µL",
            defaultMinRef = 150.0,
            defaultMaxRef = 400.0,
            vulgarization = "Fragments cellulaires essentiels pour la coagulation sanguine et la cicatrisation des vaisseaux.",
            recommendation = "Prévenir tout risque de saignement anormal si le taux est bas."
        ),

        // --- GLYCÉMIE & MÉTABOLISME ---
        BiomarkerDefinition(
            standardName = "Glycémie à jeun",
            keywords = listOf("glycémie", "glycemie", "glucose"),
            defaultUnit = "g/L",
            defaultMinRef = 0.70,
            defaultMaxRef = 1.05,
            vulgarization = "Taux de sucre circulant dans le sang après le jeûne de la nuit. Dépiste le diabète.",
            recommendation = "Privilégier les fibres, sucres complexes et une activité physique régulière."
        ),
        BiomarkerDefinition(
            standardName = "Hémoglobine Glyquée (HbA1c)",
            keywords = listOf("hba1c", "hémoglobine glyquée", "hemoglobine glyquee"),
            defaultUnit = "%",
            defaultMinRef = 4.0,
            defaultMaxRef = 6.0,
            vulgarization = "Moyenne de votre équilibre glycémique au cours des 3 derniers mois.",
            recommendation = "Objectif généralement fixé en dessous de 6.5% à 7% selon prescription médicale."
        ),

        // --- BILAN LIPIDIQUE (CHOLESTÉROL & TRIGLYCÉRIDES) ---
        BiomarkerDefinition(
            standardName = "Cholestérol Total",
            keywords = listOf("cholestérol total", "cholesterol total"),
            defaultUnit = "g/L",
            defaultMinRef = 1.50,
            defaultMaxRef = 2.00,
            vulgarization = "Total des graisses circulantes nécessaires aux parois cellulaires mais à équilibrer.",
            recommendation = "Adopter une alimentation protectrice de type méditerranéen."
        ),
        BiomarkerDefinition(
            standardName = "Cholestérol LDL",
            keywords = listOf("cholestérol ldl", "cholesterol ldl", "ldl-c", "ldl"),
            defaultUnit = "g/L",
            defaultMinRef = 0.0,
            defaultMaxRef = 1.30,
            vulgarization = "Fraction du cholestérol pouvant former des plaques d'athérome sur les parois artérielles.",
            recommendation = "Réduire les graisses saturées d'origine animale et faire 30 min de marche quotidienne."
        ),
        BiomarkerDefinition(
            standardName = "Cholestérol HDL",
            keywords = listOf("cholestérol hdl", "cholesterol hdl", "hdl-c", "hdl"),
            defaultUnit = "g/L",
            defaultMinRef = 0.40,
            defaultMaxRef = 0.90,
            vulgarization = "Le 'bon' cholestérol épurateur ramenant l'excès de lipides vers le foie.",
            recommendation = "Un taux élevé constitue un facteur protecteur cardiovasculaire reconnu."
        ),
        BiomarkerDefinition(
            standardName = "Triglycérides",
            keywords = listOf("triglycérides", "triglycerides", "tg"),
            defaultUnit = "g/L",
            defaultMinRef = 0.50,
            defaultMaxRef = 1.50,
            vulgarization = "Graisses de réserve issues de l'assimilation des sucres rapides et des graisses alimentaires.",
            recommendation = "Limiter l'alcool, les sodas et les pâtisseries pour normaliser ce taux."
        ),

        // --- BILAN RÉNAL ---
        BiomarkerDefinition(
            standardName = "Créatinine",
            keywords = listOf("créatinine", "creatinine", "creat"),
            defaultUnit = "µmol/L",
            defaultMinRef = 60.0,
            defaultMaxRef = 110.0,
            vulgarization = "Déchet de l'activité musculaire éliminé par les reins. Révèle la qualité du filtre rénal.",
            recommendation = "Boire régulièrement 1,5 litre d'eau par jour pour soutenir l'élimination rénale."
        ),
        BiomarkerDefinition(
            standardName = "Débit de Filtration Glomérulaire (DFG)",
            keywords = listOf("dfg", "clairance", "ckd-epi", "mdrd"),
            defaultUnit = "mL/min/1.73m2",
            defaultMinRef = 60.0,
            defaultMaxRef = 120.0,
            vulgarization = "Estimation de la capacité de filtration globale de vos reins (valeur normale > 60).",
            recommendation = "Une valeur au-dessus de 60 mL/min témoigne d'une bonne fonction rénale."
        ),
        BiomarkerDefinition(
            standardName = "Urée",
            keywords = listOf("urée", "uree"),
            defaultUnit = "mmol/L",
            defaultMinRef = 2.5,
            defaultMaxRef = 7.5,
            vulgarization = "Produit de dégradation des protéines par le foie, évacué par voie rénale.",
            recommendation = "S'élève souvent en cas de déshydratation passagère ou d'apport carné abondant."
        ),
        BiomarkerDefinition(
            standardName = "Acide Urique",
            keywords = listOf("acide urique", "uricemie", "uricémie"),
            defaultUnit = "mg/L",
            defaultMinRef = 30.0,
            defaultMaxRef = 70.0,
            vulgarization = "Substance issue de la dégradation des purines. Responsable des crises de goutte si trop élevée.",
            recommendation = "Limiter les viandes rouges, gibiers, bières et charcuteries."
        ),

        // --- BILAN HÉPATIQUE (FOIE) ---
        BiomarkerDefinition(
            standardName = "Transaminases ALAT (TGP)",
            keywords = listOf("alat", "tgp", "transaminases alat"),
            defaultUnit = "UI/L",
            defaultMinRef = 5.0,
            defaultMaxRef = 45.0,
            vulgarization = "Enzyme hépatique hautement spécifique servant à évaluer l'intégrité des cellules du foie.",
            recommendation = "Une élévation temporaire peut résulter de certains médicaments ou d'efforts intenses."
        ),
        BiomarkerDefinition(
            standardName = "Transaminases ASAT (TGO)",
            keywords = listOf("asat", "tgo", "transaminases asat"),
            defaultUnit = "UI/L",
            defaultMinRef = 5.0,
            defaultMaxRef = 35.0,
            vulgarization = "Enzyme présente dans le foie, les muscles et le cœur.",
            recommendation = "À corréler avec les ALAT pour vérifier la santé hépatique."
        ),
        BiomarkerDefinition(
            standardName = "Gamma GT (GGT)",
            keywords = listOf("gamma gt", "gamma-gt", "ggt"),
            defaultUnit = "UI/L",
            defaultMinRef = 10.0,
            defaultMaxRef = 55.0,
            vulgarization = "Enzyme hépato-biliaire sensible à l'engorgement biliaire et aux prises médicamenteuses.",
            recommendation = "Éviter les toxiques hépatiques et privilégier une hydratation saine."
        ),
        BiomarkerDefinition(
            standardName = "Phosphatases Alcalines (PAL)",
            keywords = listOf("phosphatases alcalines", "pal"),
            defaultUnit = "UI/L",
            defaultMinRef = 35.0,
            defaultMaxRef = 105.0,
            vulgarization = "Enzymes situées dans les voies biliaires et les os.",
            recommendation = "Permet de surveiller l'écoulement de la bile et la minéralisation osseuse."
        ),
        BiomarkerDefinition(
            standardName = "Bilirubine Totale",
            keywords = listOf("bilirubine totale", "bilirubine"),
            defaultUnit = "mg/L",
            defaultMinRef = 2.0,
            defaultMaxRef = 12.0,
            vulgarization = "Pigment jaune issu de la dégradation physiologique de l'hémoglobine.",
            recommendation = "Une augmentation peut provoquer un ictère (jaunisse) bénin (ex: syndrome de Gilbert)."
        ),

        // --- IONOGRAMME & MINÉRAUX ---
        BiomarkerDefinition(
            standardName = "Sodium (Natrémie)",
            keywords = listOf("sodium", "natremie", "natrémie", "na+"),
            defaultUnit = "mmol/L",
            defaultMinRef = 136.0,
            defaultMaxRef = 145.0,
            vulgarization = "Électrolyte régulant l'équilibre en eau entre l'intérieur et l'extérieur des cellules.",
            recommendation = "Équilibre hydrique satisfaisant."
        ),
        BiomarkerDefinition(
            standardName = "Potassium (Kaliémie)",
            keywords = listOf("potassium", "kaliemie", "kaliémie", "k+"),
            defaultUnit = "mmol/L",
            defaultMinRef = 3.5,
            defaultMaxRef = 5.0,
            vulgarization = "Minéral indispensable à la conduction nerveuse et au rythme cardiaque.",
            recommendation = "Le potassium sanguin est strictement surveillé pour prévenir les troubles cardiaques."
        ),
        BiomarkerDefinition(
            standardName = "Chlore",
            keywords = listOf("chlore", "chlorémie", "chloremie", "cl-"),
            defaultUnit = "mmol/L",
            defaultMinRef = 98.0,
            defaultMaxRef = 107.0,
            vulgarization = "Accompagne le sodium dans le maintien de la pression osmotique et de l'acidité sanguine.",
            recommendation = "Équilibre acido-basique préservé."
        ),
        BiomarkerDefinition(
            standardName = "Calcium (Calcémie)",
            keywords = listOf("calcium", "calcémie", "calcemie", "ca"),
            defaultUnit = "mg/L",
            defaultMinRef = 85.0,
            defaultMaxRef = 105.0,
            vulgarization = "Minéral capital pour la solidité des os, des dents et la transmission neuro-musculaire.",
            recommendation = "Consommer des sources calciques (laitages, eaux minérales calciques, amandes)."
        ),
        BiomarkerDefinition(
            standardName = "Ferritine",
            keywords = listOf("ferritine"),
            defaultUnit = "ng/mL",
            defaultMinRef = 30.0,
            defaultMaxRef = 300.0,
            vulgarization = "Réserves profondes en fer de votre organisme dans le foie et la moelle.",
            recommendation = "Indispensable pour éviter la fatigue liée à l'anémie ferriprive."
        ),
        BiomarkerDefinition(
            standardName = "Fer Sérique",
            keywords = listOf("fer sérique", "fer serique", "fer"),
            defaultUnit = "µg/dL",
            defaultMinRef = 50.0,
            defaultMaxRef = 170.0,
            vulgarization = "Quantité de fer circulant dans le sang à un instant donné.",
            recommendation = "À interpréter conjointement avec le taux de ferritine."
        ),

        // --- INFLAMMATION & IMMUNITÉ ---
        BiomarkerDefinition(
            standardName = "Protéine C-Réactive (CRP)",
            keywords = listOf("crp", "protéine c-réactive", "proteine c reactive", "c-reactive"),
            defaultUnit = "mg/L",
            defaultMinRef = 0.0,
            defaultMaxRef = 5.0,
            vulgarization = "Protéine du foie augmentant très rapidement lors de toute inflammation ou infection active.",
            recommendation = "Une CRP < 5 mg/L exclut généralement un syndrome inflammatoire aigu important."
        ),
        BiomarkerDefinition(
            standardName = "Vitesse de Sédimentation (VS)",
            keywords = listOf("vitesse de sédimentation", "vitesse de sedimentation", "vs"),
            defaultUnit = "mm",
            defaultMinRef = 2.0,
            defaultMaxRef = 20.0,
            vulgarization = "Indicateur indirect de la vitesse de chute des globules rouges en cas d'inflammation.",
            recommendation = "Permet de suivre l'évolution d'une réaction inflammatoire au fil du temps."
        ),

        // --- ENDOCRINOLOGIE & VITAMINES ---
        BiomarkerDefinition(
            standardName = "TSH (Thyréostimuline)",
            keywords = listOf("tsh", "thyréostimuline", "thyreostimuline", "tsh ultra-sensible"),
            defaultUnit = "mUI/L",
            defaultMinRef = 0.40,
            defaultMaxRef = 4.00,
            vulgarization = "Hormone hypophysaire régulant le fonctionnement de la glande thyroïde (métabolisme global).",
            recommendation = "Un taux équilibré garantit le bon niveau d'énergie, de poids et de rythme cardiaque."
        ),
        BiomarkerDefinition(
            standardName = "Vitamine D (25-OH)",
            keywords = listOf("vitamine d", "25-oh-vitamine d", "vit d"),
            defaultUnit = "ng/mL",
            defaultMinRef = 30.0,
            defaultMaxRef = 100.0,
            vulgarization = "Vitamine capitale pour l'absorption du calcium, l'immunité et la santé musculaire.",
            recommendation = "Exposition solaire modérée et supplémentation recommandée en période hivernale."
        ),
        BiomarkerDefinition(
            standardName = "Vitamine B12",
            keywords = listOf("vitamine b12", "b12", "cobalamine"),
            defaultUnit = "pg/mL",
            defaultMinRef = 200.0,
            defaultMaxRef = 900.0,
            vulgarization = "Vitamine essentielle à la formation des globules rouges et à la protection des neurones.",
            recommendation = "Primordiale pour prévenir l'anémie mégaloblastique et la fatigue nerveuse."
        )
    )

    /**
     * Analyse robuste de l'ensemble du texte extrait par OCR (multi-pages).
     * Reconnaît les paramètres connus du dictionnaire médical ET les formats tabulaires libres.
     * Prend en compte la langue de vulgarisation ("fr", "en", "ar") configurée par l'utilisateur.
     */
    fun parse(ocrText: String, language: String = "fr"): LabReport {
        val extractedBiomarkers = mutableListOf<Biomarker>()
        val alreadyMatchedNames = mutableSetOf<String>()
        val lines = ocrText.lines()

        // 1. Détection des métadonnées du document (Labo, Date, Titre)
        val extractedLab = extractLaboratory(ocrText)
        val extractedDate = extractReportDate(ocrText)
        val extractedTitle = extractReportTitle(ocrText)

        // 2. Passe 1 : Recherche structurée via le dictionnaire médical exhaustif
        for (definition in medicalDictionary) {
            val biomarker = findBiomarkerInText(lines, definition)
            if (biomarker != null && !alreadyMatchedNames.contains(biomarker.name)) {
                extractedBiomarkers.add(biomarker)
                alreadyMatchedNames.add(biomarker.name)
            }
        }

        // 3. Passe 2 : Détection dynamique de paramètres additionnels non listés (pattern: Nom + Valeur + Unité + Bornes)
        val dynamicBiomarkers = extractDynamicBiomarkers(lines, alreadyMatchedNames)
        for (bio in dynamicBiomarkers) {
            if (!alreadyMatchedNames.contains(bio.name)) {
                extractedBiomarkers.add(bio)
                alreadyMatchedNames.add(bio.name)
            }
        }

        // 4. Synthèse globale clinique
        val abnormalCount = extractedBiomarkers.count { it.isAbnormal }
        val doctorSummary = if (extractedBiomarkers.isEmpty()) {
            "Analyse du document effectuée. Pour une précision optimale sur les bilans manuscrits ou atypiques, vous pouvez également renseigner une clé Gemini API."
        } else if (abnormalCount > 0) {
            "Bilan comprenant ${extractedBiomarkers.size} paramètres analysés. $abnormalCount biomarqueur(s) se situent en dehors des valeurs de référence du laboratoire et nécessitent un avis médical."
        } else {
            "Bilan comprenant ${extractedBiomarkers.size} paramètres analysés. L'ensemble des marqueurs biologiques mesurés est dans les normes de référence du laboratoire."
        }

        val rawReport = LabReport(
            id = "report_scan_${UUID.randomUUID()}",
            title = extractedTitle,
            date = extractedDate,
            laboratory = extractedLab,
            biomarkers = extractedBiomarkers,
            doctorSummary = doctorSummary,
            hasAlerts = abnormalCount > 0
        )

        return BiomarkerLocalizationHelper.localizeReport(rawReport, language)
    }

    /**
     * Tente de localiser un biomarqueur dans les lignes du texte
     * en inspectant strictement la même ligne que le mot-clé pour éviter
     * d'attraper un nombre d'une autre ligne.
     */
    private fun findBiomarkerInText(
        lines: List<String>,
        def: BiomarkerDefinition
    ): Biomarker? {
        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase()

            // Vérifie si l'un des mots-clés est présent
            val matchedKeyword = def.keywords.firstOrNull { kw ->
                lowerLine.contains(Regex("""\b${Regex.escape(kw)}\b""")) || lowerLine.contains(kw)
            } ?: continue

            // Extraction de la valeur numérique uniquement sur la même ligne
            val parsedResult = extractMeasurementFromWindow(
                window = line,
                keyword = matchedKeyword,
                defaultUnit = def.defaultUnit,
                defaultMin = def.defaultMinRef,
                defaultMax = def.defaultMaxRef
            )

            if (parsedResult != null) {
                val (value, unit, minRef, maxRef) = parsedResult
                val status = when {
                    maxRef > 0 && value > maxRef -> BiomarkerStatus.HIGH
                    minRef > 0 && value < minRef -> BiomarkerStatus.LOW
                    else -> BiomarkerStatus.NORMAL
                }

                return Biomarker(
                    id = "bio_parsed_${UUID.randomUUID().toString().take(6)}",
                    name = def.standardName,
                    value = value,
                    unit = unit,
                    minReference = minRef,
                    maxReference = maxRef,
                    status = status,
                    vulgarization = def.vulgarization,
                    recommendation = def.recommendation
                )
            }
        }
        return null
    }

    /**
     * Détection dynamique des lignes comportant une unité médicale standard
     * pour extraire les biomarqueurs rares qui ne figurent pas dans le dictionnaire.
     */
    private fun extractDynamicBiomarkers(
        lines: List<String>,
        alreadyMatched: Set<String>
    ): List<Biomarker> {
        val results = mutableListOf<Biomarker>()
        // Unités médicales françaises & internationales courantes
        val unitPattern = Regex("""(?i)\b(g/L|mg/L|µmol/L|mmol/L|UI/L|mUI/L|10\^3/µL|10\^9/L|10\^6/µL|ng/mL|pg/mL|g/dL|mg/dL|fL|pg|%|mL/min)\b""")

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isBlank() || line.length < 5) continue

            val unitMatch = unitPattern.find(line)
            if (unitMatch != null) {
                val unit = unitMatch.value
                val beforeUnit = line.substring(0, unitMatch.range.first).trim()

                // Recherche du chiffre juste avant l'unité
                val valRegex = Regex("""([0-9]+([.,][0-9]+)?)\s*$""")
                val valMatch = valRegex.find(beforeUnit)
                if (valMatch != null) {
                    val value = valMatch.value.replace(',', '.').toDoubleOrNull() ?: continue
                    val rawName = beforeUnit.substring(0, valMatch.range.first)
                        .replace(Regex("""[:=.\-_|]+$"""), "")
                        .trim()

                    if (rawName.length in 3..40 && !rawName.any { it.isDigit() } && !alreadyMatched.contains(rawName)) {
                        // Recherche de bornes de référence après l'unité (ex: "(0.50 - 1.20)")
                        val afterUnit = line.substring(unitMatch.range.last + 1).trim()
                        val boundsRegex = Regex("""([0-9]+([.,][0-9]+)?)\s*[-àa]\s*([0-9]+([.,][0-9]+)?)""")
                        val boundsMatch = boundsRegex.find(afterUnit)

                        var minRef = 0.0
                        var maxRef = 0.0
                        if (boundsMatch != null) {
                            minRef = boundsMatch.groupValues[1].replace(',', '.').toDoubleOrNull() ?: 0.0
                            maxRef = boundsMatch.groupValues[3].replace(',', '.').toDoubleOrNull() ?: 0.0
                        }

                        val status = when {
                            maxRef > 0 && value > maxRef -> BiomarkerStatus.HIGH
                            minRef > 0 && value < minRef -> BiomarkerStatus.LOW
                            else -> BiomarkerStatus.NORMAL
                        }

                        val formattedName = rawName.split(" ")
                            .filter { it.isNotBlank() }
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

                        results.add(
                            Biomarker(
                                id = "bio_dyn_${UUID.randomUUID().toString().take(6)}",
                                name = formattedName,
                                value = value,
                                unit = unit,
                                minReference = minRef,
                                maxReference = maxRef,
                                status = status,
                                vulgarization = "Paramètre biologique extrait de votre compte-rendu de laboratoire.",
                                recommendation = "À évaluer en consultation médicale avec votre praticien."
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    /**
     * Analyse une fenêtre textuelle combinée (ligne + suivantes) pour extraire
     * la valeur mesurée et optionnellement les bornes de référence réelles du laboratoire.
     */
    private fun extractMeasurementFromWindow(
        window: String,
        keyword: String,
        defaultUnit: String,
        defaultMin: Double,
        defaultMax: Double
    ): MeasurementResult? {
        // Supprime les dates pour ne pas prendre le jour ou l'année comme valeur (ex: 21/11/2026)
        val sanitizedWindow = window.replace(Regex("""\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b"""), " ")
            .replace(Regex("""(?i)\b(dossier|n°|page)\s*:?\s*\d+\b"""), " ")

        // Trouve la position du mot clé
        val kwIndex = sanitizedWindow.lowercase().indexOf(keyword.lowercase())
        if (kwIndex == -1) return null

        val textAfterKeyword = sanitizedWindow.substring(kwIndex + keyword.length)

        // Cherche le premier nombre pertinent après le mot-clé avec support des décimales à virgule ou point
        val numberRegex = Regex("""([0-9]+([.,][0-9]+)?)""")
        val matches = numberRegex.findAll(textAfterKeyword).toList()

        if (matches.isEmpty()) return null

        val primaryValue = matches[0].value.replace(',', '.').toDoubleOrNull() ?: return null

        // Tente d'extraire les bornes imprimées du laboratoire (ex: "Normes : 13.0 - 17.0" ou "(13.0 - 17.0)")
        var minRef = defaultMin
        var maxRef = defaultMax

        if (matches.size >= 3) {
            val potentialMin = matches[1].value.replace(',', '.').toDoubleOrNull()
            val potentialMax = matches[2].value.replace(',', '.').toDoubleOrNull()
            if (potentialMin != null && potentialMax != null && potentialMax > potentialMin) {
                minRef = potentialMin
                maxRef = potentialMax
            }
        }

        // Tente de trouver l'unité réelle dans le texte ou utilise l'unité standard
        val unitRegex = Regex("""(?i)\b(g/L|mg/L|µmol/L|mmol/L|UI/L|mUI/L|10\^3/µL|10\^9/L|10\^6/µL|ng/mL|pg/mL|g/dL|mg/dL|fL|pg|%|mL/min)\b""")
        val matchedUnit = unitRegex.find(textAfterKeyword)?.value ?: defaultUnit

        return MeasurementResult(
            value = primaryValue,
            unit = matchedUnit,
            minRef = minRef,
            maxRef = maxRef
        )
    }

    private fun extractLaboratory(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("pasteur") -> "Laboratoire Pasteur"
            lower.contains("cerba") -> "Laboratoire Cerba"
            lower.contains("biomnis") -> "Eurofins Biomnis"
            lower.contains("synlab") -> "Synlab Médical"
            lower.contains("inovie") -> "Groupe Inovie"
            lower.contains("bioesterel") -> "Laboratoire Bioesterel"
            lower.contains("biogroup") -> "Biogroup LCD"
            else -> {
                // Recherche d'une ligne contenant "Laboratoire"
                val labLine = text.lines().firstOrNull { it.contains("laboratoire", ignoreCase = true) }
                labLine?.trim()?.take(40) ?: "Laboratoire d'Analyses Médicales"
            }
        }
    }

    private fun extractReportDate(text: String): String {
        // Recherche d'une date jj/mm/aaaa
        val dateRegex = Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{4})\b""")
        val match = dateRegex.find(text)
        if (match != null) {
            val (day, month, year) = match.destructured
            return "$year-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
        }
        return SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).format(Date())
    }

    private fun extractReportTitle(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("hématologie") || lower.contains("numération") || lower.contains("nfs") -> "Bilan Hématologique & NFS"
            lower.contains("biochimie") || lower.contains("glycémie") -> "Bilan Biochimique & Sanguin"
            lower.contains("lipidique") || lower.contains("cholestérol") -> "Bilan Lipidique & Cardiovasculaire"
            lower.contains("thyro") || lower.contains("tsh") -> "Bilan Thyroïdien & Hormonal"
            lower.contains("rénal") || lower.contains("créatinine") -> "Bilan Rénal & Ionique"
            else -> "Bilan d'Analyses Biologiques"
        }
    }

    private data class MeasurementResult(
        val value: Double,
        val unit: String,
        val minRef: Double,
        val maxRef: Double
    )
}
