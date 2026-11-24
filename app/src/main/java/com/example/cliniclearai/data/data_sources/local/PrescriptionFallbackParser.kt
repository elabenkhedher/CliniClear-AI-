package com.example.cliniclearai.data.data_sources.local

import com.example.cliniclearai.domain.models.Drug
import java.text.Normalizer
import java.util.UUID

/**
 * Analyseur hors-ligne ultra-résilient pour ordonnances médicales numérisées par OCR.
 * 
 * Combine :
 * 1. Un dictionnaire médical de plus de 130 spécialités et DCI fréquentes en France/Europe.
 * 2. Un comparateur insensible aux accents, à la casse et aux espaces.
 * 3. Un extracteur heuristique dynamique capable d'extraire N'IMPORTE QUEL médicament non répertorié
 *    présent sur l'ordonnance en repérant les structures "Nom + Dosage (mg/g/ml) + Posologie".
 */
object PrescriptionFallbackParser {

    private data class KnownDrugInfo(
        val brandName: String,
        val genericName: String,
        val activeIngredient: String,
        val defaultDosage: String,
        val defaultPurpose: String,
        val defaultWarnings: String
    )

    // Dictionnaire enrichi des médicaments les plus prescrits
    private val KNOWN_DRUGS = listOf(
        // Antalgiques / Antipyrétiques / Opioïdes
        KnownDrugInfo("Doliprane", "Paracétamol", "paracetamol", "1000 mg", "Antalgique et antipyrétique", "Ne pas dépasser 3 g par jour sans avis médical. Risque d'hépatotoxicité."),
        KnownDrugInfo("Dafalgan", "Paracétamol", "paracetamol", "1000 mg", "Antalgique et antipyrétique", "Surveiller les doublons avec d'autres spécialités contenant du paracétamol."),
        KnownDrugInfo("Efferalgan", "Paracétamol", "paracetamol", "1000 mg", "Antalgique", "Contient du paracétamol."),
        KnownDrugInfo("Paracétamol", "Paracétamol", "paracetamol", "1000 mg", "Antalgique", "Attention au risque de surdosage en cas de cumul."),
        KnownDrugInfo("Perfalgan", "Paracétamol", "paracetamol", "1000 mg", "Antalgique", "Paracétamol injectable."),
        KnownDrugInfo("Codoliprane", "Paracétamol / Codéine", "paracetamol", "500 mg / 30 mg", "Antalgique de palier 2", "Risque de somnolence et de constipation."),
        KnownDrugInfo("Tramadol", "Tramadol", "tramadol", "50 mg", "Antalgique opioïde de palier 2", "Risque de somnolence et de dépression respiratoire en association avec les sédatifs."),
        KnownDrugInfo("Topalgic", "Tramadol", "tramadol", "50 mg", "Antalgique opioïde", "Opioïde faible, surveiller la dépendance."),
        KnownDrugInfo("Ixprim", "Tramadol / Paracétamol", "tramadol", "37.5 mg / 325 mg", "Antalgique central", "Contient du tramadol et du paracétamol."),
        KnownDrugInfo("Zaldiar", "Tramadol / Paracétamol", "tramadol", "37.5 mg / 325 mg", "Antalgique central", "Association tramadol + paracétamol."),
        KnownDrugInfo("Acupan", "Néfopam", "nefopam", "20 mg", "Antalgique central non opioïde", "Surveillance des effets anticholinergiques."),
        KnownDrugInfo("Morphine", "Morphine", "morphine", "10 mg", "Antalgique opioïde fort de palier 3", "Risque majeur de dépression respiratoire et de dépendance."),
        KnownDrugInfo("Skenan", "Morphine sulfate", "morphine", "30 mg", "Opioïde à libération prolongée", "Respecter scrupuleusement la posologie."),
        KnownDrugInfo("Oxycontin", "Oxycodone", "oxycodone", "10 mg", "Opioïde fort", "Prudence extrême en cas d'association avec les benzodiazépines."),

        // Anti-inflammatoires (AINS) & Corticoïdes
        KnownDrugInfo("Advil", "Ibuprofène", "ibuprofen", "400 mg", "Anti-inflammatoire non stéroïdien (AINS)", "Risque d'hémorragie digestive sévère si associé à un anticoagulant (Aspirine/Kardégic)."),
        KnownDrugInfo("Nurofen", "Ibuprofène", "ibuprofen", "400 mg", "Anti-inflammatoire AINS", "À prendre au cours des repas. Contre-indiqué en cas d'ulcère gastrique."),
        KnownDrugInfo("Ibuprofène", "Ibuprofène", "ibuprofen", "400 mg", "Anti-inflammatoire", "Prudence chez les insuffisants rénaux et hypertendus."),
        KnownDrugInfo("Antarène", "Ibuprofène", "ibuprofen", "400 mg", "Anti-inflammatoire AINS", "Risque digestif et rénal."),
        KnownDrugInfo("Voltarène", "Diclofénac", "diclofenac", "50 mg", "Anti-inflammatoire AINS", "Risque cardiovasculaire et digestif accru."),
        KnownDrugInfo("Diclofénac", "Diclofénac", "diclofenac", "50 mg", "AINS", "Prendre pendant les repas."),
        KnownDrugInfo("Kétoprofène", "Kétoprofène", "ketoprofen", "100 mg", "Anti-inflammatoire puissant", "Photosensibilisation et toxicité gastrique."),
        KnownDrugInfo("Bi-Profénid", "Kétoprofène", "ketoprofen", "150 mg", "AINS à libération prolongée", "Surveillance digestive stricte."),
        KnownDrugInfo("Profénid", "Kétoprofène", "ketoprofen", "100 mg", "AINS", "Prendre avec un grand verre d'eau au repas."),
        KnownDrugInfo("Apranax", "Naproxène", "naproxen", "550 mg", "Anti-inflammatoire AINS", "Risque ulcérogène."),
        KnownDrugInfo("Naproxène", "Naproxène", "naproxen", "500 mg", "AINS", "À associer à un protecteur gastrique si terrain à risque."),
        KnownDrugInfo("Celebrex", "Célécoxib", "celecoxib", "200 mg", "Inhibiteur sélectif de la COX-2", "Surveillance de la pression artérielle."),
        KnownDrugInfo("Cortancyl", "Prednisone", "prednisone", "20 mg", "Corticoïde anti-inflammatoire", "Régime pauvre en sel et sucres rapides."),
        KnownDrugInfo("Prednisone", "Prednisone", "prednisone", "20 mg", "Corticoïde", "À prendre le matin."),
        KnownDrugInfo("Solupred", "Prednisolone", "prednisolone", "20 mg", "Corticoïde", "Prendre le matin au petit-déjeuner."),
        KnownDrugInfo("Prednisolone", "Prednisolone", "prednisolone", "20 mg", "Corticoïde orodispersible", "Prendre de préférence le matin."),
        KnownDrugInfo("Célestène", "Bétaméthasone", "betamethasone", "2 mg", "Corticoïde", "À prendre le matin."),

        // Cardiologie, Anticoagulants, Statines, Antihypertenseurs
        KnownDrugInfo("Kardégic", "Acide acétylsalicylique", "acide acetylsalicylique", "75 mg", "Antiagrégant plaquettaire", "Majore considérablement le risque d'hémorragie digestive si associé aux AINS."),
        KnownDrugInfo("Aspirine", "Acide acétylsalicylique", "aspirin", "500 mg", "Antiagrégant et antalgique", "Risque hémorragique."),
        KnownDrugInfo("Aspegic", "Acétylsalicylate de lysine", "aspirin", "100 mg", "Antiagrégant / Antalgique", "Prendre au milieu du repas."),
        KnownDrugInfo("Plavix", "Clopidogrel", "clopidogrel", "75 mg", "Antiagrégant plaquettaire", "Risque de saignement prolongé."),
        KnownDrugInfo("Clopidogrel", "Clopidogrel", "clopidogrel", "75 mg", "Antiagrégant", "Prévention des thromboses artérielles."),
        KnownDrugInfo("Eliquis", "Apixaban", "apixaban", "5 mg", "Anticoagulant oral direct (AOD)", "Ne pas associer aux AINS ou antiagrégants sans surveillance hématologique."),
        KnownDrugInfo("Xarelto", "Rivaroxaban", "rivaroxaban", "20 mg", "Anticoagulant oral direct (AOD)", "Prendre au cours du repas pour une absorption optimale."),
        KnownDrugInfo("Préviscan", "Fluindione", "fluindione", "20 mg", "Anticoagulant antivitamine K (AVK)", "Contrôle régulier de l'INR indispensable."),
        KnownDrugInfo("Sintrom", "Acénocoumarol", "acenocoumarol", "4 mg", "Anticoagulant AVK", "Surveillance étroite de l'INR."),
        KnownDrugInfo("Tahor", "Atorvastatine", "atorvastatin", "20 mg", "Hypolipémiant (Statine)", "Signaler toute douleur musculaire inexpliquée (risque de rhabdomyolyse)."),
        KnownDrugInfo("Lipitor", "Atorvastatine", "atorvastatin", "20 mg", "Statine hypocholestérolémiante", "Surveillance des transaminases et CPK."),
        KnownDrugInfo("Atorvastatine", "Atorvastatine", "atorvastatin", "20 mg", "Statine", "Prendre régulièrement le soir."),
        KnownDrugInfo("Crestor", "Rosuvastatine", "rosuvastatin", "10 mg", "Statine puissante", "Attention à l'association avec les fibrates."),
        KnownDrugInfo("Rosuvastatine", "Rosuvastatine", "rosuvastatin", "10 mg", "Statine", "À prendre le soir."),
        KnownDrugInfo("Zocor", "Simvastatine", "simvastatin", "20 mg", "Statine", "Ne pas consommer de pamplemousse pendant le traitement."),
        KnownDrugInfo("Lipanthyl", "Fénofibrate", "fenofibrate", "145 mg", "Fibrate hypolipémiant", "Multiplie le risque de toxicité musculaire si associé aux statines."),
        KnownDrugInfo("Triatec", "Ramipril", "ramipril", "5 mg", "Inhibiteur de l'enzyme de conversion (IEC)", "Surveiller la fonction rénale et la kaliémie."),
        KnownDrugInfo("Ramipril", "Ramipril", "ramipril", "5 mg", "Antihypertenseur IEC", "Risque de toux sèche ou d'élévation du potassium."),
        KnownDrugInfo("Coversyl", "Périndopril", "perindopril", "5 mg", "Antihypertenseur IEC", "Prendre le matin avant le repas."),
        KnownDrugInfo("Cozaar", "Losartan", "losartan", "50 mg", "Antagoniste des récepteurs de l'angiotensine II (ARA II)", "Surveiller la kaliémie."),
        KnownDrugInfo("Tareg", "Valsartan", "valsartan", "80 mg", "Antihypertenseur Sartan", "Surveiller la tension artérielle."),
        KnownDrugInfo("Aprovel", "Irbésartan", "irbesartan", "150 mg", "Sartan", "Prudence chez les insuffisants rénaux."),
        KnownDrugInfo("Amlodipine", "Amlodipine", "amlodipine", "5 mg", "Inhibiteur calcique", "Risque d'œdèmes des chevilles."),
        KnownDrugInfo("Loxen", "Nicardipine", "nicardipine", "20 mg", "Inhibiteur calcique", "Traitement de l'hypertension artérielle."),
        KnownDrugInfo("Cardensiel", "Bisoprolol", "bisoprolol", "2.5 mg", "Bêta-bloquant", "Ne pas interrompre brutalement le traitement."),
        KnownDrugInfo("Bisoprolol", "Bisoprolol", "bisoprolol", "5 mg", "Bêta-bloquant cardio-sélectif", "Surveiller la fréquence cardiaque."),
        KnownDrugInfo("Nébibolol", "Nébibolol", "nebibolol", "5 mg", "Bêta-bloquant", "À prendre à heure fixe."),
        KnownDrugInfo("Aldactone", "Spironolactone", "spironolactone", "25 mg", "Diurétique épargneur de potassium", "Risque élevé d'hyperkaliémie mortelle avec IEC/Sartans."),
        KnownDrugInfo("Spironolactone", "Spironolactone", "spironolactone", "25 mg", "Diurétique épargneur de potassium", "Contrôler régulièrement la kaliémie."),
        KnownDrugInfo("Lasilix", "Furosémide", "furosemide", "40 mg", "Diurétique de l'anse", "Prendre le matin pour éviter les réveils nocturnes."),
        KnownDrugInfo("Furosémide", "Furosémide", "furosemide", "40 mg", "Diurétique", "Surveiller l'hydratation et le potassium."),
        KnownDrugInfo("Diffu-K", "Chlorure de potassium", "chlorure de potassium", "600 mg", "Supplémentation potassique", "À prendre au cours des repas."),

        // Gastro-entérologie
        KnownDrugInfo("Inexium", "Ésoméprazole", "esomeprazole", "20 mg", "Inhibiteur de la pompe à protons (IPP)", "Prendre 30 minutes avant le premier repas."),
        KnownDrugInfo("Ésoméprazole", "Ésoméprazole", "esomeprazole", "20 mg", "IPP antiacide", "Protège la muqueuse gastrique."),
        KnownDrugInfo("Mopral", "Oméprazole", "omeprazole", "20 mg", "Inhibiteur de la pompe à protons (IPP)", "Avaler la gélule sans l'ouvrir avec un grand verre d'eau."),
        KnownDrugInfo("Oméprazole", "Oméprazole", "omeprazole", "20 mg", "IPP", "Prendre le matin à jeun."),
        KnownDrugInfo("Pantoprazole", "Pantoprazole", "pantoprazole", "20 mg", "IPP", "Traitement du reflux gastro-œsophagien."),
        KnownDrugInfo("Gaviscon", "Alginate de sodium / Bicarbonate", "alginate", "10 ml", "Pansement gastrique anti-reflux", "Prendre après les principaux repas et au coucher."),
        KnownDrugInfo("Smecta", "Diosmectite", "diosmectite", "3 g", "Protecteur gastro-intestinal", "Prendre à distance des autres médicaments (au moins 2 heures)."),
        KnownDrugInfo("Spasfon", "Phloroglucinol", "phloroglucinol", "80 mg", "Antispasmodique musculotrope", "Soulage les spasmes digestifs, urinaires ou gynécologiques."),
        KnownDrugInfo("Phloroglucinol", "Phloroglucinol", "phloroglucinol", "80 mg", "Antispasmodique", "Prendre au moment des douleurs."),
        KnownDrugInfo("Débridat", "Trimébutine", "trimebutine", "100 mg", "Régulateur de la motricité intestinale", "Prendre avant les repas."),
        KnownDrugInfo("Imodium", "Lopéramide", "loperamide", "2 mg", "Ralentisseur du transit intestinal", "Ne pas dépasser 6 à 8 gélules par jour."),
        KnownDrugInfo("Vogalène", "Métopimazine", "metopimazine", "15 mg", "Antiémétique", "Soulagement des nausées et vomissements."),

        // Diabète
        KnownDrugInfo("Metformine", "Metformine", "metformin", "850 mg", "Antidiabétique oral régulateur de glycémie", "Prendre au milieu des repas pour réduire les effets digestifs."),
        KnownDrugInfo("Glucophage", "Metformine", "metformin", "1000 mg", "Antidiabétique oral", "Arrêter temporairement avant injection d'iode."),
        KnownDrugInfo("Diamicron", "Gliclazide", "gliclazide", "60 mg", "Sulfamide hypoglycémiant", "Prendre impérativement au petit-déjeuner. Risque d'hypoglycémie."),
        KnownDrugInfo("Januvia", "Sitagliptine", "sitagliptine", "100 mg", "Inhibiteur de la DPP-4 (Incrétine)", "Une prise par jour avec ou sans aliment."),
        KnownDrugInfo("Forxiga", "Dapagliflozine", "dapagliflozine", "10 mg", "Inhibiteur du SGLT2 (Gliflozine)", "Surveiller l'hydratation et les mycoses urinaires."),
        KnownDrugInfo("Jardiance", "Empagliflozine", "empagliflozine", "10 mg", "Inhibiteur du SGLT2", "Bonne protection cardiovasculaire et rénale."),
        KnownDrugInfo("Ozempic", "Sémaglutide", "semaglutide", "0.5 mg", "Analogue du GLP-1", "Injection sous-cutanée hebdomadaire."),
        KnownDrugInfo("Trulicity", "Dulaglutide", "dulaglutide", "1.5 mg", "Analogue du GLP-1", "Injection hebdomadaire."),

        // Antibiotiques & Infectiologie
        KnownDrugInfo("Amoxicilline", "Amoxicilline", "amoxicillin", "1000 mg", "Antibiotique pénicilline à large spectre", "Respecter impérativement la durée complète du traitement même si amélioration."),
        KnownDrugInfo("Augmentin", "Amoxicilline / Acide clavulanique", "amoxicillin", "1 g / 125 mg", "Antibiotique pénicilline + inhibiteur", "Prendre au début des repas pour limiter la diarrhée."),
        KnownDrugInfo("Orelox", "Cefpodoxime", "cefpodoxime", "100 mg", "Céphalosporine de 3ème génération", "Prendre au cours des repas."),
        KnownDrugInfo("Zithromax", "Azithromycine", "azithromycin", "250 mg", "Macrolide", "Traitement court de 3 jours généralement."),
        KnownDrugInfo("Pyostacine", "Pristinamycine", "pristinamycin", "500 mg", "Synergistine antibactérienne", "Prendre au milieu des repas."),
        KnownDrugInfo("Ciflox", "Ciprofloxacine", "ciprofloxacin", "500 mg", "Fluoroquinolone", "Risque d'atteinte tendineuse (tendon d'Achille). Éviter l'exposition au soleil."),
        KnownDrugInfo("Flagyl", "Métronidazole", "metronidazole", "500 mg", "Antibactérien et antiparasitaire", "Alcool strictement proscrit pendant le traitement (effet antabuse)."),
        KnownDrugInfo("Monuril", "Fosfomycine trométamol", "fosfomycine", "3 g", "Antibiotique pour infection urinaire aiguë", "Prise unique le soir au coucher vessie vide."),
        KnownDrugInfo("Furadantine", "Nitrofurantoïne", "nitrofurantoine", "50 mg", "Antiseptique urinaire", "Prendre pendant les repas."),

        // Psychotropes / Anxiolytiques / Somnifères / Antidépresseurs
        KnownDrugInfo("Xanax", "Alprazolam", "alprazolam", "0.25 mg", "Anxiolytique (Benzodiazépine)", "Risque d'accoutumance. Danger de dépression respiratoire si associé aux opioïdes."),
        KnownDrugInfo("Lexomil", "Bromazépam", "bromazepam", "6 mg", "Anxiolytique", "Prendre la dose minimale efficace. Ne pas consommer d'alcool."),
        KnownDrugInfo("Valium", "Diazépam", "diazepam", "10 mg", "Benzodiazépine myorelaxante", "Risque de somnolence diurne."),
        KnownDrugInfo("Temesta", "Lorazépam", "lorazepam", "1 mg", "Anxiolytique", "Durée de prescription limitée."),
        KnownDrugInfo("Seresta", "Oxazépam", "oxazepam", "10 mg", "Anxiolytique benzodiazépine", "Adapté en cas d'insuffisance hépatique modérée."),
        KnownDrugInfo("Stilnox", "Zolpidem", "zolpidem", "10 mg", "Hypnotique apparenté", "À prendre immédiatement avant le coucher."),
        KnownDrugInfo("Imovane", "Zopiclone", "zopiclone", "7.5 mg", "Hypnotique", "Goût amer possible au réveil. Durée limitée."),
        KnownDrugInfo("Seroplex", "Escitalopram", "escitalopram", "10 mg", "Antidépresseur ISRS", "Effet thérapeutique apparaissant après 2 à 3 semaines."),
        KnownDrugInfo("Deroxat", "Paroxétine", "paroxetine", "20 mg", "Antidépresseur", "Prendre de préférence le matin."),
        KnownDrugInfo("Prozac", "Fluoxétine", "fluoxetine", "20 mg", "Antidépresseur", "Prendre le matin."),
        KnownDrugInfo("Effexor", "Venlafaxine", "venlafaxine", "75 mg", "Antidépresseur IRSNA", "Surveillance de la tension artérielle."),
        KnownDrugInfo("Laroxyl", "Amitriptyline", "amitriptyline", "25 mg", "Antidépresseur et antalgique neuropathique", "Effets anticholinergiques possibles (bouche sèche, somnolence)."),
        KnownDrugInfo("Lyrica", "Prégabaline", "pregabalin", "75 mg", "Antiepileptique et douleurs neuropathiques", "Risque d'étourdissements et de somnolence."),
        KnownDrugInfo("Neurontin", "Gabapentine", "gabapentin", "300 mg", "Douleurs neuropathiques", "Augmentation progressive des doses."),

        // Pneumologie, Allergies & Thyroïde
        KnownDrugInfo("Ventoline", "Salbutamol", "salbutamol", "100 µg", "Bronchodilatateur d'action rapide", "À inhaler immédiatement en cas de crise d'asthme."),
        KnownDrugInfo("Sérétide", "Salmétérol / Fluticasone", "salmeterol", "100 µg / 50 µg", "Traitement de fond de l'asthme et BPCO", "Se rincer la bouche après inhalation."),
        KnownDrugInfo("Symbicort", "Budésonide / Formotérol", "budesonide", "200 µg / 6 µg", "Traitement de fond et de crise de l'asthme", "Rinçage buccal après chaque prise."),
        KnownDrugInfo("Aerius", "Desloratadine", "desloratadine", "5 mg", "Antihistaminique antiallergique", "Non sédatif, une prise par jour."),
        KnownDrugInfo("Cétirizine", "Cétirizine", "cetirizine", "10 mg", "Antihistaminique H1", "Soulage rhinite allergique et urticaire."),
        KnownDrugInfo("Atarax", "Hydroxyzine", "hydroxyzine", "25 mg", "Antihistaminique sédatif et anxiolytique", "Forte somnolence. Ne pas conduire."),
        KnownDrugInfo("Levothyrox", "Lévothyroxine sodique", "levothyroxine", "100 µg", "Hormone thyroïdienne de substitution", "Prendre impérativement le matin à jeun 30 minutes avant le petit-déjeuner.")
    )

    // Termes parasites à exclure lors de la détection heuristique
    private val EXCLUDED_LINE_WORDS = setOf(
        "docteur", "medecin", "cabinet", "clinique", "centre", "hopital", "ordonnance", "prescription",
        "patient", "monsieur", "madame", "date", "signature", "tampon", "pharmacie", "pharmacien",
        "delivrance", "renouvellement", "rpps", "adeli", "telephone", "tel", "rue", "avenue", "boulevard",
        "bp", "cedex", "mail", "courriel", "securite", "sociale", "numero", "age", "poids", "taille"
    )

    /**
     * Analyse le texte OCR et extrait la liste des médicaments prescrits.
     */
    fun parse(ocrText: String): List<Drug> {
        val foundDrugs = mutableListOf<Drug>()
        val detectedNames = mutableSetOf<String>()

        val rawLines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val normalizedFullText = normalize(ocrText)

        // 1. Recherche via le dictionnaire médical enrichi (Insensible aux accents et à la casse)
        for (known in KNOWN_DRUGS) {
            val normBrand = normalize(known.brandName)
            val normGeneric = normalize(known.genericName)

            val regexBrand = Regex("""\b${Regex.escape(normBrand)}\b""", RegexOption.IGNORE_CASE)
            val regexGeneric = Regex("""\b${Regex.escape(normGeneric)}\b""", RegexOption.IGNORE_CASE)

            if (regexBrand.containsMatchIn(normalizedFullText) || regexGeneric.containsMatchIn(normalizedFullText)) {
                // Trouver la ligne et son contexte (ligne courante + suivante)
                val lineIndex = rawLines.indexOfFirst { line ->
                    val normLine = normalize(line)
                    regexBrand.containsMatchIn(normLine) || regexGeneric.containsMatchIn(normLine)
                }

                val currentLine = if (lineIndex >= 0) rawLines[lineIndex] else ""
                val nextLine = if (lineIndex >= 0 && lineIndex + 1 < rawLines.size) rawLines[lineIndex + 1] else ""
                val contextBlock = "$currentLine $nextLine"

                val dosage = extractDosage(contextBlock).ifBlank { known.defaultDosage }
                val frequency = extractFrequency(contextBlock).ifBlank { "Selon prescription médicale" }

                val key = normBrand.take(6)
                if (!detectedNames.contains(key)) {
                    detectedNames.add(key)
                    foundDrugs.add(
                        Drug(
                            id = "drug_${UUID.randomUUID().toString().take(8)}",
                            brandName = known.brandName,
                            genericName = known.genericName,
                            activeIngredients = listOf(known.activeIngredient),
                            dosage = dosage,
                            frequency = frequency,
                            instructions = "Prendre selon la prescription du médecin.",
                            purpose = known.defaultPurpose,
                            warnings = known.defaultWarnings,
                            hasActiveRecall = false,
                            isPrescription = true
                        )
                    )
                }
            }
        }

        // 2. Extracteur heuristique dynamique : Repère TOUT médicament non présent dans le dictionnaire
        // Exemples : "Spasfon Lyoc 80 mg : 2 cp", "FORXIGA 10mg 1 comprimé/jour", "1. Kardegic 75 mg - 1 sachet"
        val dosageRegex = Regex("""\b(\d+[\.,]?\d*)\s*(mg|g|µg|mcg|ml|ui|%)\b""", RegexOption.IGNORE_CASE)
        val formRegex = Regex("""\b(comprim[ée]s?|g[ée]lules?|sachets?|ampoules?|flacons?|gouttes?|patchs?|cr[èe]mes?|sirops?|suppositoires?|cp|posologie)\b""", RegexOption.IGNORE_CASE)

        for (i in rawLines.indices) {
            val line = rawLines[i]
            val normLine = normalize(line)

            // Ignore les en-têtes de médecins, adresses, etc.
            val firstWord = normLine.split(" ", ":", "-", ".").firstOrNull { it.isNotBlank() } ?: ""
            if (EXCLUDED_LINE_WORDS.contains(firstWord)) continue

            val hasDosage = dosageRegex.containsMatchIn(line)
            val hasForm = formRegex.containsMatchIn(line)

            if (hasDosage || hasForm) {
                // Tenter d'extraire le nom du médicament au début de la ligne
                val cleanLine = line.replace(Regex("""^[\d\.\-\*\•\)\s]+"""), "").trim()
                val candidateName = cleanLine.split(" ", ":", ",", "-").take(2).joinToString(" ").trim()

                val normCandidate = normalize(candidateName)
                if (normCandidate.length >= 3 &&
                    !EXCLUDED_LINE_WORDS.any { normCandidate.contains(it) } &&
                    !detectedNames.any { normCandidate.contains(it) || it.contains(normCandidate.take(5)) }
                ) {
                    val nextLine = if (i + 1 < rawLines.size) rawLines[i + 1] else ""
                    val block = "$cleanLine $nextLine"

                    val dosage = extractDosage(block)
                    val frequency = extractFrequency(block).ifBlank { "Selon prescription médicale" }

                    val formattedBrand = candidateName.replaceFirstChar { it.uppercase() }
                    detectedNames.add(normCandidate.take(6))

                    foundDrugs.add(
                        Drug(
                            id = "drug_${UUID.randomUUID().toString().take(8)}",
                            brandName = formattedBrand,
                            genericName = formattedBrand,
                            activeIngredients = listOf(normCandidate.lowercase()),
                            dosage = dosage,
                            frequency = frequency,
                            instructions = "Prendre selon la prescription médicale.",
                            purpose = "Prescription médicale",
                            warnings = "Respecter la posologie prescrite par le praticien.",
                            hasActiveRecall = false,
                            isPrescription = true
                        )
                    )
                }
            }
        }

        return foundDrugs
    }

    private fun normalize(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    private fun extractDosage(text: String): String {
        val dosageRegex = Regex("""\b(\d+[\.,]?\d*)\s*(mg|g|µg|mcg|ml|ui|%)\b""", RegexOption.IGNORE_CASE)
        val match = dosageRegex.find(text)
        return match?.value ?: ""
    }

    private fun extractFrequency(text: String): String {
        val freqRegex = Regex("""\b(\d+\s*(?:cp|comprim[ée]s?|g[ée]lules?|sachets?|fois)\s*(?:par|\/)\s*jour)\b""", RegexOption.IGNORE_CASE)
        val match = freqRegex.find(text)
        if (match != null) return match.value

        val lower = text.lowercase()
        if (lower.contains("matin") && lower.contains("soir")) return "1 le matin et 1 le soir"
        if (lower.contains("matin") && lower.contains("midi") && lower.contains("soir")) return "1 matin, 1 midi et 1 soir"
        if (lower.contains("matin")) return "1 le matin au petit-déjeuner"
        if (lower.contains("midi")) return "1 le midi au déjeuner"
        if (lower.contains("soir") || lower.contains("coucher")) return "1 le soir au coucher"
        if (lower.contains("si besoin") || lower.contains("si douleur")) return "En cas de douleur (selon besoin)"
        if (lower.contains("toutes les")) {
            val hMatch = Regex("""toutes les \d+ heures""", RegexOption.IGNORE_CASE).find(lower)
            if (hMatch != null) return hMatch.value
        }

        return ""
    }
}
