package com.example.cliniclearai.data.data_sources.local

import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.LabReport

/**
 * Gestionnaire de localisation et de vulgarisation médicale multilingue (Français, English, العربية).
 * Permet d'adapter dynamiquement les noms, explications simples et recommandations
 * selon la préférence linguistique choisie par l'utilisateur.
 */
object BiomarkerLocalizationHelper {

    data class LocalizedText(
        val name: String,
        val vulgarization: String,
        val recommendation: String
    )

    // Clé unique canonique pour chaque biomarqueur standard
    private val standardBiomarkersMap = mapOf(
        "hemoglobine" to mapOf(
            "fr" to LocalizedText(
                name = "Hémoglobine",
                vulgarization = "Protéine des globules rouges chargée de transporter l'oxygène dans tout le corps.",
                recommendation = "Consommer des aliments riches en fer et vitamine C pour favoriser l'oxygénation tissulaire."
            ),
            "en" to LocalizedText(
                name = "Hemoglobin",
                vulgarization = "Red blood cell protein responsible for carrying oxygen throughout the entire body.",
                recommendation = "Consume foods rich in iron and vitamin C to promote tissue oxygenation."
            ),
            "ar" to LocalizedText(
                name = "الهيموغلوبين",
                vulgarization = "بروتين في خلايا الدم الحمراء مسؤول عن نقل الأكسجين إلى جميع خلايا الجسم.",
                recommendation = "تناول أطعمة غنية بالحديد وفيتامين ج لتعزيز وصول الأكسجين للأنسجة."
            )
        ),
        "hematies" to mapOf(
            "fr" to LocalizedText(
                name = "Hématies (Globules Rouges)",
                vulgarization = "Cellules sanguines transportant l'oxygène depuis les poumons vers tous les organes.",
                recommendation = "À surveiller en cas de fatigue inexpliquée ou de pâleur."
            ),
            "en" to LocalizedText(
                name = "Red Blood Cells (Erythrocytes)",
                vulgarization = "Blood cells carrying oxygen from the lungs to all organs.",
                recommendation = "Monitor in case of unexplained fatigue or pale skin."
            ),
            "ar" to LocalizedText(
                name = "كريات الدم الحمراء",
                vulgarization = "خلايا دموية تنقل الأكسجين من الرئتين إلى سائر أعضاء الجسم.",
                recommendation = "تنبغي المتابعة في حال الشعور بالتعب غير المبرر أو شحوب الوجه."
            )
        ),
        "hematocrite" to mapOf(
            "fr" to LocalizedText(
                name = "Hématocrite",
                vulgarization = "Pourcentage du volume sanguin occupé par les globules rouges.",
                recommendation = "Assurer une bonne hydratation quotidienne."
            ),
            "en" to LocalizedText(
                name = "Hematocrit",
                vulgarization = "Percentage of total blood volume occupied by red blood cells.",
                recommendation = "Maintain good daily hydration."
            ),
            "ar" to LocalizedText(
                name = "الهيماتوكريت",
                vulgarization = "النسبة المئوية لحجم خلايا الدم الحمراء مقارنة بإجمالي حجم الدم.",
                recommendation = "الحرص على شرب كميات كافية من الماء يومياً."
            )
        ),
        "vgm" to mapOf(
            "fr" to LocalizedText(
                name = "VGM (Volume Globulaire Moyen)",
                vulgarization = "Indicateur de la taille moyenne de vos globules rouges (dépistage anémie micro/macrocytaire).",
                recommendation = "Un taux anormal peut orienter vers une carence martiale ou vitaminique (B9/B12)."
            ),
            "en" to LocalizedText(
                name = "MCV (Mean Corpuscular Volume)",
                vulgarization = "Indicator of the average size of your red blood cells (screens for micro/macrocytic anemia).",
                recommendation = "An abnormal level may indicate an iron or vitamin (B9/B12) deficiency."
            ),
            "ar" to LocalizedText(
                name = "الحجم الكروي الوسطي (MCV)",
                vulgarization = "مؤشر لمتوسط حجم خلايا الدم الحمراء (للكشف عن أنواع فقر الدم المختلفة).",
                recommendation = "قد يشير المستوى غير الطبيعي إلى نقص في الحديد أو فيتامينات ب9/ب12."
            )
        ),
        "tcmh" to mapOf(
            "fr" to LocalizedText(
                name = "TCMH",
                vulgarization = "Quantité moyenne d'hémoglobine contenue dans chaque globule rouge.",
                recommendation = "Participe au diagnostic de la qualité des globules rouges."
            ),
            "en" to LocalizedText(
                name = "MCH (Mean Corpuscular Hemoglobin)",
                vulgarization = "Average amount of hemoglobin contained in each red blood cell.",
                recommendation = "Helps assess the quality and oxygen-carrying capacity of red blood cells."
            ),
            "ar" to LocalizedText(
                name = "هيموغلوبين الكرية الوسطي (MCH)",
                vulgarization = "متوسط كمية الهيموغلوبين الموجودة في كل خلية دم حمراء.",
                recommendation = "يساعد في تقييم جودة وكفاءة خلايا الدم الحمراء."
            )
        ),
        "ccmh" to mapOf(
            "fr" to LocalizedText(
                name = "CCMH",
                vulgarization = "Concentration en hémoglobine à l'intérieur des globules rouges.",
                recommendation = "Indicateur de chromie sanguine."
            ),
            "en" to LocalizedText(
                name = "MCHC",
                vulgarization = "Concentration of hemoglobin inside red blood cells.",
                recommendation = "Useful indicator of blood cell color and saturation."
            ),
            "ar" to LocalizedText(
                name = "التركيز الوسطي للهيموغلوبين (MCHC)",
                vulgarization = "تركيز الهيموغلوبين داخل خلايا الدم الحمراء.",
                recommendation = "مؤشر هام لتقييم صبغة وتشبع خلايا الدم."
            )
        ),
        "leucocytes" to mapOf(
            "fr" to LocalizedText(
                name = "Leucocytes (Globules Blancs)",
                vulgarization = "Cellules maîtresses de votre système immunitaire protégeant contre infections et inflammations.",
                recommendation = "Une augmentation temporaire reflète souvent une réponse normale à une infection."
            ),
            "en" to LocalizedText(
                name = "White Blood Cells (Leukocytes)",
                vulgarization = "Key cells of your immune system protecting against infections and inflammation.",
                recommendation = "A temporary elevation often reflects a normal immune response to infection."
            ),
            "ar" to LocalizedText(
                name = "كريات الدم البيضاء",
                vulgarization = "الخلايا الرئيسية في جهاز المناعة لحماية الجسم من العدوى والالتهابات.",
                recommendation = "الارتفاع المؤقت يعكس غالباً استجابة مناعية طبيعية لمكافحة عدوى."
            )
        ),
        "neutrophiles" to mapOf(
            "fr" to LocalizedText(
                name = "Polynucléaires Neutrophiles",
                vulgarization = "Première ligne de défense bactérienne du système immunitaire.",
                recommendation = "Marqueur réactif en cas d'infection bactérienne ou d'inflammation."
            ),
            "en" to LocalizedText(
                name = "Neutrophils",
                vulgarization = "First line of defense against bacterial infections in the immune system.",
                recommendation = "Reactive marker during bacterial infections or acute inflammation."
            ),
            "ar" to LocalizedText(
                name = "الخلايا الحبيبية المتعادلة",
                vulgarization = "خط الدفاع الأول في الجهاز المناعي لمكافحة العدوى البكتيرية.",
                recommendation = "مؤشر يتفاعل سريعاً عند حدوث عدوى بكتيرية أو التهاب حاد."
            )
        ),
        "lymphocytes" to mapOf(
            "fr" to LocalizedText(
                name = "Lymphocytes",
                vulgarization = "Cellules immunitaires spécialisées dans la lutte contre les virus et la mémoire immunitaire.",
                recommendation = "Un taux élevé accompagne souvent les épisodes viraux récents."
            ),
            "en" to LocalizedText(
                name = "Lymphocytes",
                vulgarization = "Immune cells specialized in fighting viral infections and immune memory.",
                recommendation = "An elevated level often accompanies recent viral episodes."
            ),
            "ar" to LocalizedText(
                name = "الخلايا اللمفاوية",
                vulgarization = "خلايا مناعية متخصصة في مكافحة الفيروسات وتشكيل الذاكرة المناعية.",
                recommendation = "غالباً ما يصاحب ارتفاع نسبتها التعافي من نوبات فيروسية حديثة."
            )
        ),
        "monocytes" to mapOf(
            "fr" to LocalizedText(
                name = "Monocytes",
                vulgarization = "Cellules éboueuses nettoyant les débris cellulaires et combattant les agents pathogènes.",
                recommendation = "S'élèvent lors de la phase de guérison de nombreuses infections."
            ),
            "en" to LocalizedText(
                name = "Monocytes",
                vulgarization = "Scavenger cells cleaning up cellular debris and fighting pathogens.",
                recommendation = "Often increase during the recovery phase of many infections."
            ),
            "ar" to LocalizedText(
                name = "الخلايا الوحيدة",
                vulgarization = "خلايا مناعية تبتلع المخلفات الخلوية وتكافح مسببات الأمراض.",
                recommendation = "ترتفع نسبتها عادة خلال مرحلة الشفاء والتعافي من العدوى."
            )
        ),
        "eosinophiles" to mapOf(
            "fr" to LocalizedText(
                name = "Éosinophiles",
                vulgarization = "Cellules impliquées dans les réactions allergiques et la défense antiparasitaire.",
                recommendation = "À surveiller en cas de terrain allergique (asthme, eczéma) ou séjour en zone tropicale."
            ),
            "en" to LocalizedText(
                name = "Eosinophils",
                vulgarization = "Cells involved in allergic reactions and defense against parasites.",
                recommendation = "Monitor in case of allergies (asthma, eczema) or recent travel to tropical areas."
            ),
            "ar" to LocalizedText(
                name = "الخلايا الحمضية",
                vulgarization = "خلايا تشارك في التفاعلات التحسسية ومقاومة الطفيليات.",
                recommendation = "تنبغي المتابعة في حال وجود حساسية (ربو، إكزيما) أو سفر لمناطق استوائية."
            )
        ),
        "basophiles" to mapOf(
            "fr" to LocalizedText(
                name = "Basophiles",
                vulgarization = "Cellules participant à la libération d'histamine lors de réactions immunitaires.",
                recommendation = "Taux généralement très faible à l'état normal."
            ),
            "en" to LocalizedText(
                name = "Basophils",
                vulgarization = "Cells participating in histamine release during inflammatory immune reactions.",
                recommendation = "Levels are normally very low in healthy individuals."
            ),
            "ar" to LocalizedText(
                name = "الخلايا القاعدية",
                vulgarization = "خلايا تشارك في إفراز الهستامين أثناء التفاعلات التحسسية والالتهابية.",
                recommendation = "نسبتها تكون منخفضة جداً بشكل طبيعي في الحالة الصحية المعتادة."
            )
        ),
        "plaquettes" to mapOf(
            "fr" to LocalizedText(
                name = "Plaquettes",
                vulgarization = "Fragments cellulaires essentiels pour la coagulation sanguine et la cicatrisation des vaisseaux.",
                recommendation = "Prévenir tout risque de saignement anormal si le taux est bas."
            ),
            "en" to LocalizedText(
                name = "Platelets",
                vulgarization = "Cell fragments essential for blood clotting and vessel healing.",
                recommendation = "Prevent abnormal bleeding risks if levels are below reference range."
            ),
            "ar" to LocalizedText(
                name = "الصفائح الدموية",
                vulgarization = "أجزاء خلوية بالغة الأهمية لتخثر الدم والتئام الأوعية الدموية.",
                recommendation = "يجب تجنب مخاطر النزيف واستشارة الطبيب إذا كان المعدل منخفضاً."
            )
        ),
        "glycemie" to mapOf(
            "fr" to LocalizedText(
                name = "Glycémie à jeun",
                vulgarization = "Taux de sucre circulant dans le sang après le jeûne de la nuit. Dépiste le diabète.",
                recommendation = "Privilégier les fibres, sucres complexes et une activité physique régulière."
            ),
            "en" to LocalizedText(
                name = "Fasting Blood Glucose",
                vulgarization = "Level of sugar circulating in the blood after an overnight fast. Screens for diabetes.",
                recommendation = "Prioritize dietary fiber, complex carbs, and regular physical activity."
            ),
            "ar" to LocalizedText(
                name = "سكر الدم الصائم",
                vulgarization = "مستوى السكر المتداول في الدم بعد صيام الليل، ويُستخدم لتشخيص السكري.",
                recommendation = "يُوصى بتناول الألياف والسكريات المعقدة وممارسة النشاط البدني بانتظام."
            )
        ),
        "hba1c" to mapOf(
            "fr" to LocalizedText(
                name = "Hémoglobine Glyquée (HbA1c)",
                vulgarization = "Moyenne de votre équilibre glycémique au cours des 3 derniers mois.",
                recommendation = "Objectif généralement fixé en dessous de 6.5% à 7% selon prescription médicale."
            ),
            "en" to LocalizedText(
                name = "Glycated Hemoglobin (HbA1c)",
                vulgarization = "Average reflection of your blood sugar control over the past 3 months.",
                recommendation = "Target is generally set below 6.5% to 7% according to medical advice."
            ),
            "ar" to LocalizedText(
                name = "السكر التراكمي (HbA1c)",
                vulgarization = "معدل يعكس متوسط توازن السكر في الدم على مدار الأشهر الثلاثة الماضية.",
                recommendation = "المعدل المستهدف يكون عادة أقل من 6.5% إلى 7% حسب إرشادات الطبيب."
            )
        ),
        "cholesterol_total" to mapOf(
            "fr" to LocalizedText(
                name = "Cholestérol Total",
                vulgarization = "Total des graisses circulantes nécessaires aux parois cellulaires mais à équilibrer.",
                recommendation = "Adopter une alimentation protectrice de type méditerranéen."
            ),
            "en" to LocalizedText(
                name = "Total Cholesterol",
                vulgarization = "Total amount of circulating fats needed for cell membranes, requiring healthy balance.",
                recommendation = "Adopt a heart-healthy Mediterranean-style diet."
            ),
            "ar" to LocalizedText(
                name = "الكوليسترول الكلي",
                vulgarization = "إجمالي الدهون المتداولة اللازمة لجدران الخلايا والتي يجب الحفاظ على توازنها.",
                recommendation = "يُوصى باتباع نمط غذائي صحي ومتوازن يشبه حمية البحر الأبيض المتوسط."
            )
        ),
        "cholesterol_ldl" to mapOf(
            "fr" to LocalizedText(
                name = "Cholestérol LDL",
                vulgarization = "Fraction du cholestérol pouvant former des plaques d'athérome sur les parois artérielles.",
                recommendation = "Réduire les graisses saturées d'origine animale et faire 30 min de marche quotidienne."
            ),
            "en" to LocalizedText(
                name = "LDL Cholesterol",
                vulgarization = "Cholesterol fraction that can deposit plaques inside arterial walls ('bad' cholesterol).",
                recommendation = "Reduce saturated animal fats and engage in 30 minutes of daily walking."
            ),
            "ar" to LocalizedText(
                name = "الكوليسترول الضار (LDL)",
                vulgarization = "الكوليسترول 'الضار' الذي قد يترسب ويشكل لويحات دهنية على جدران الشرايين.",
                recommendation = "الحد من الدهون المشبعة الحيوانية وممارسة المشي لمدة 30 دقيقة يومياً."
            )
        ),
        "cholesterol_hdl" to mapOf(
            "fr" to LocalizedText(
                name = "Cholestérol HDL",
                vulgarization = "Le 'bon' cholestérol épurateur ramenant l'excès de lipides vers le foie.",
                recommendation = "Un taux élevé constitue un facteur protecteur cardiovasculaire reconnu."
            ),
            "en" to LocalizedText(
                name = "HDL Cholesterol",
                vulgarization = "The 'good' scavenger cholesterol carrying excess lipids back to the liver.",
                recommendation = "A high level serves as a recognized protective factor for the cardiovascular system."
            ),
            "ar" to LocalizedText(
                name = "الكوليسترول الجيد (HDL)",
                vulgarization = "الكوليسترول 'الجيد' الذي ينقل الدهون الزائدة من الأنسجة إلى الكبد لمعالجتها.",
                recommendation = "المستوى المرتفع يعد عاملاً وقائياً هاماً لصحة القلب والأوعية الدموية."
            )
        ),
        "triglycerides" to mapOf(
            "fr" to LocalizedText(
                name = "Triglycérides",
                vulgarization = "Graisses de réserve issues de l'assimilation des sucres rapides et des graisses alimentaires.",
                recommendation = "Limiter l'alcool, les sodas et les pâtisseries pour normaliser ce taux."
            ),
            "en" to LocalizedText(
                name = "Triglycerides",
                vulgarization = "Storage fats derived from rapid sugars and dietary fat intake.",
                recommendation = "Limit alcohol, sugary sodas, and pastries to normalize this level."
            ),
            "ar" to LocalizedText(
                name = "الدهون الثلاثية",
                vulgarization = "دهون تخزينية تنتج عن هضم وتخزين السكريات السريعة والدهون الغذائية.",
                recommendation = "يُوصى بتقليل المشروبات السكرية والحلويات والدهون لضبط المعدل."
            )
        ),
        "creatinine" to mapOf(
            "fr" to LocalizedText(
                name = "Créatinine",
                vulgarization = "Déchet de l'activité musculaire éliminé par les reins. Révèle la qualité du filtre rénal.",
                recommendation = "Boire régulièrement 1,5 litre d'eau par jour pour soutenir l'élimination rénale."
            ),
            "en" to LocalizedText(
                name = "Creatinine",
                vulgarization = "Waste product from muscle metabolism eliminated by kidneys. Indicates kidney filtration.",
                recommendation = "Drink 1.5 liters of water daily to support healthy kidney clearance."
            ),
            "ar" to LocalizedText(
                name = "الكرياتينين",
                vulgarization = "مادة ناتجة عن النشاط العضلي وتتخلص منها الكلى، وتدل على كفاءة عمل الكلى.",
                recommendation = "شرب 1.5 لتر من الماء بانتظام يومياً لدعم وظائف التصفية الكلوية."
            )
        ),
        "dfg" to mapOf(
            "fr" to LocalizedText(
                name = "Débit de Filtration Glomérulaire (DFG)",
                vulgarization = "Estimation de la capacité de filtration globale de vos reins (valeur normale > 60).",
                recommendation = "Une valeur au-dessus de 60 mL/min témoigne d'une bonne fonction rénale."
            ),
            "en" to LocalizedText(
                name = "eGFR (Glomerular Filtration Rate)",
                vulgarization = "Estimation of the overall filtration capacity of your kidneys (normal > 60).",
                recommendation = "A score above 60 mL/min indicates preserved renal function."
            ),
            "ar" to LocalizedText(
                name = "معدل الترشيح الكبيبي (DFG)",
                vulgarization = "تقدير لقدرة الكليتين الإجمالية على تنقية الدم وتصفيته (المعدل الطبيعي > 60).",
                recommendation = "القيمة التي تفوق 60 مل/دقيقة تدل على كفاءة جيدة لوظائف الكلى."
            )
        ),
        "uree" to mapOf(
            "fr" to LocalizedText(
                name = "Urée",
                vulgarization = "Produit de dégradation des protéines par le foie, évacué par voie rénale.",
                recommendation = "S'élève souvent en cas de déshydratation passagère ou d'apport carné abondant."
            ),
            "en" to LocalizedText(
                name = "Blood Urea Nitrogen (BUN)",
                vulgarization = "Breakdown product of proteins by the liver, excreted through the kidneys.",
                recommendation = "Often rises during mild dehydration or high protein/meat consumption."
            ),
            "ar" to LocalizedText(
                name = "اليوريا",
                vulgarization = "مخلفات هضم البروتينات في الكبد ويتم التخلص منها عبر الكلى.",
                recommendation = "قد يرتفع في حالات الجفاف العابر أو الإكثار من تناول اللحوم والبروتينات."
            )
        ),
        "acide_urique" to mapOf(
            "fr" to LocalizedText(
                name = "Acide Urique",
                vulgarization = "Substance issue de la dégradation des purines. Responsable des crises de goutte si trop élevée.",
                recommendation = "Limiter les viandes rouges, gibiers, bières et charcuteries."
            ),
            "en" to LocalizedText(
                name = "Uric Acid",
                vulgarization = "Substance resulting from the breakdown of purines. May cause gout flares when high.",
                recommendation = "Limit red meat, seafood, alcohol/beer, and rich cold cuts."
            ),
            "ar" to LocalizedText(
                name = "حمض اليوريك (حمض البول)",
                vulgarization = "مادة تنتج عن تكسير مركبات البيورين، وارتفاعها قد يسبب نوبات النقرس.",
                recommendation = "الحد من اللحوم الحمراء والمأكولات البحرية والمشروبات السكرية."
            )
        ),
        "alat" to mapOf(
            "fr" to LocalizedText(
                name = "Transaminases ALAT (TGP)",
                vulgarization = "Enzyme hépatique hautement spécifique servant à évaluer l'intégrité des cellules du foie.",
                recommendation = "Une élévation temporaire peut résulter de certains médicaments ou d'efforts intenses."
            ),
            "en" to LocalizedText(
                name = "ALT (SGPT) Transaminase",
                vulgarization = "Liver enzyme highly specific for assessing liver cell integrity.",
                recommendation = "A mild temporary elevation can be caused by medications or intense physical exercise."
            ),
            "ar" to LocalizedText(
                name = "إنزيم الكبد ALAT (TGP)",
                vulgarization = "إنزيم كبدي عالي الخصوصية يُستخدم لتقييم سلامة خلايا الكبد.",
                recommendation = "الارتفاع المؤقت قد ينجم عن بعض الأدوية أو المجهود البدني الشديد."
            )
        ),
        "asat" to mapOf(
            "fr" to LocalizedText(
                name = "Transaminases ASAT (TGO)",
                vulgarization = "Enzyme présente dans le foie, les muscles et le cœur.",
                recommendation = "À corréler avec les ALAT pour vérifier la santé hépatique."
            ),
            "en" to LocalizedText(
                name = "AST (SGOT) Transaminase",
                vulgarization = "Enzyme present in liver, heart, and muscle tissue.",
                recommendation = "Interpreted alongside ALT to evaluate hepatic and muscular health."
            ),
            "ar" to LocalizedText(
                name = "إنزيم الكبد ASAT (TGO)",
                vulgarization = "إنزيم يتواجد في أنسجة الكبد والعضلات والقلب.",
                recommendation = "تتم قراءته بالمقارنة مع إنزيم ALAT للتأكد من سلامة الكبد."
            )
        ),
        "ggt" to mapOf(
            "fr" to LocalizedText(
                name = "Gamma GT (GGT)",
                vulgarization = "Enzyme hépato-biliaire sensible à l'engorgement biliaire et aux prises médicamenteuses.",
                recommendation = "Éviter les toxiques hépatiques et privilégier une hydratation saine."
            ),
            "en" to LocalizedText(
                name = "Gamma-GT (GGT)",
                vulgarization = "Liver-biliary enzyme sensitive to bile flow issues and toxic/medication exposure.",
                recommendation = "Avoid hepatic irritants and prioritize clean daily hydration."
            ),
            "ar" to LocalizedText(
                name = "غاما جي تي (GGT)",
                vulgarization = "إنزيم كبدي صفراوي حساس للركود الصفراوي وتأثير الأدوية والمواد السامة.",
                recommendation = "تجنب المواد المرهقة للكبد والحرص على شرب الماء والسوائل الصحية."
            )
        ),
        "pal" to mapOf(
            "fr" to LocalizedText(
                name = "Phosphatases Alcalines (PAL)",
                vulgarization = "Enzymes situées dans les voies biliaires et les os.",
                recommendation = "Permet de surveiller l'écoulement de la bile et la minéralisation osseuse."
            ),
            "en" to LocalizedText(
                name = "Alkaline Phosphatase (ALP)",
                vulgarization = "Enzymes located in the bile ducts and skeletal bone tissue.",
                recommendation = "Helps monitor bile drainage and healthy bone remodeling."
            ),
            "ar" to LocalizedText(
                name = "الفوسفاتاز القلوي (PAL)",
                vulgarization = "إنزيمات تتواجد بشكل رئيسي في القنوات الصفراوية والعظام.",
                recommendation = "تساعد في متابعة تدفق العصارة الصفراوية وصحة العظام."
            )
        ),
        "bilirubine" to mapOf(
            "fr" to LocalizedText(
                name = "Bilirubine Totale",
                vulgarization = "Pigment jaune issu de la dégradation physiologique de l'hémoglobine.",
                recommendation = "Une augmentation peut provoquer un ictère (jaunisse) bénin (ex: syndrome de Gilbert)."
            ),
            "en" to LocalizedText(
                name = "Total Bilirubin",
                vulgarization = "Yellow pigment produced by the natural breakdown of hemoglobin.",
                recommendation = "Elevated levels can cause mild jaundice (e.g. benign Gilbert's syndrome)."
            ),
            "ar" to LocalizedText(
                name = "البيليروبين الكلي",
                vulgarization = "صبغة صفراء ناتجة عن التحلل الطبيعي للهيموغلوبين.",
                recommendation = "الارتفاع قد يسبب اصفراراً طفيفاً (يرقان) حميداً مثل متلازمة جيلبرت."
            )
        ),
        "sodium" to mapOf(
            "fr" to LocalizedText(
                name = "Sodium (Natrémie)",
                vulgarization = "Électrolyte régulant l'équilibre en eau entre l'intérieur et l'extérieur des cellules.",
                recommendation = "Équilibre hydrique satisfaisant."
            ),
            "en" to LocalizedText(
                name = "Sodium",
                vulgarization = "Electrolyte controlling the water balance inside and outside cells.",
                recommendation = "Maintains proper hydration and blood pressure balance."
            ),
            "ar" to LocalizedText(
                name = "الصوديوم",
                vulgarization = "معدن كهرلي يضبط توازن الماء والسوائل داخل وخارج خلايا الجسم.",
                recommendation = "يعكس توازناً مائياً ملائماً في الجسم."
            )
        ),
        "potassium" to mapOf(
            "fr" to LocalizedText(
                name = "Potassium (Kaliémie)",
                vulgarization = "Minéral indispensable à la conduction nerveuse et au rythme cardiaque.",
                recommendation = "Le potassium sanguin est strictement surveillé pour prévenir les troubles cardiaques."
            ),
            "en" to LocalizedText(
                name = "Potassium",
                vulgarization = "Vital mineral for proper nerve conduction and steady heart rhythm.",
                recommendation = "Blood potassium is strictly monitored to prevent heart rhythm disturbances."
            ),
            "ar" to LocalizedText(
                name = "البوتاسيوم",
                vulgarization = "معدن حيوي لانتظام ضربات القلب ونقل الإشارات العصبية والعضلية.",
                recommendation = "تتم مراقبة البوتاسيوم بدقة للوقاية من اضطرابات نبضات القلب."
            )
        ),
        "chlore" to mapOf(
            "fr" to LocalizedText(
                name = "Chlore",
                vulgarization = "Accompagne le sodium dans le maintien de la pression osmotique et de l'acidité sanguine.",
                recommendation = "Équilibre acido-basique préservé."
            ),
            "en" to LocalizedText(
                name = "Chloride",
                vulgarization = "Accompanies sodium to maintain osmotic pressure and acid-base blood balance.",
                recommendation = "Preserves healthy systemic electrolyte balance."
            ),
            "ar" to LocalizedText(
                name = "الكلوريد",
                vulgarization = "يرافق الصوديوم للحفاظ على التوازن الحمضي القاعدي وضغط السوائل.",
                recommendation = "يساهم في الحفاظ على توازن الكهارل في الجسم."
            )
        ),
        "calcium" to mapOf(
            "fr" to LocalizedText(
                name = "Calcium (Calcémie)",
                vulgarization = "Minéral capital pour la solidité des os, des dents et la transmission neuro-musculaire.",
                recommendation = "Consommer des sources calciques (laitages, eaux minérales calciques, amandes)."
            ),
            "en" to LocalizedText(
                name = "Calcium",
                vulgarization = "Essential mineral for bone strength, dental health, and nerve-muscle signaling.",
                recommendation = "Consume calcium sources (dairy, fortified plant milks, mineral water, almonds)."
            ),
            "ar" to LocalizedText(
                name = "الكالسيوم",
                vulgarization = "معدن أساسي لقوة العظام والأسنان ونقل الإشارات بين الأعصاب والعضلات.",
                recommendation = "تناول مصادر الكالسيوم الصحية (الألبان، المياه الغنية بالكالسيوم، اللوز)."
            )
        ),
        "ferritine" to mapOf(
            "fr" to LocalizedText(
                name = "Ferritine",
                vulgarization = "Réserves profondes en fer de votre organisme dans le foie et la moelle.",
                recommendation = "Indispensable pour éviter la fatigue liée à l'anémie ferriprive."
            ),
            "en" to LocalizedText(
                name = "Ferritin",
                vulgarization = "Deep iron reserves stored in the liver, spleen, and bone marrow.",
                recommendation = "Essential to prevent fatigue caused by iron deficiency anemia."
            ),
            "ar" to LocalizedText(
                name = "الفيريتين (مخزون الحديد)",
                vulgarization = "المخزون الرئيسي للحديد في الكبد ونخاع العظام.",
                recommendation = "مهم جداً لتفادي الإرهاق الناجم عن فقر الدم بنقص الحديد."
            )
        ),
        "fer_serique" to mapOf(
            "fr" to LocalizedText(
                name = "Fer Sérique",
                vulgarization = "Quantité de fer circulant dans le sang à un instant donné.",
                recommendation = "À interpréter conjointement avec le taux de ferritine."
            ),
            "en" to LocalizedText(
                name = "Serum Iron",
                vulgarization = "Amount of circulating iron currently traveling in the bloodstream.",
                recommendation = "Best interpreted jointly with ferritin levels for a complete picture."
            ),
            "ar" to LocalizedText(
                name = "الحديد في مصل الدم",
                vulgarization = "كمية الحديد المتداولة في مجرى الدم في الوقت الحالي.",
                recommendation = "تُفسر نتائجه جنباً إلى جنب مع فحص الفيريتين للحصول على تقييم دقيق."
            )
        ),
        "crp" to mapOf(
            "fr" to LocalizedText(
                name = "Protéine C-Réactive (CRP)",
                vulgarization = "Protéine du foie augmentant très rapidement lors de toute inflammation ou infection active.",
                recommendation = "Une CRP < 5 mg/L exclut généralement un syndrome inflammatoire aigu important."
            ),
            "en" to LocalizedText(
                name = "C-Reactive Protein (CRP)",
                vulgarization = "Liver protein that spikes quickly during any active inflammation or infection.",
                recommendation = "A CRP < 5 mg/L generally rules out significant acute systemic inflammation."
            ),
            "ar" to LocalizedText(
                name = "البروتين التفاعلي سي (CRP)",
                vulgarization = "بروتين ينتجه الكبد ويرتفع سريعاً في حالات الالتهاب أو العدوى الحادة.",
                recommendation = "مستوى CRP الأقل من 5 ملغ/لتر ينفي عموماً وجود التهاب حاد كبير."
            )
        ),
        "vs" to mapOf(
            "fr" to LocalizedText(
                name = "Vitesse de Sédimentation (VS)",
                vulgarization = "Indicateur indirect de la vitesse de chute des globules rouges en cas d'inflammation.",
                recommendation = "Permet de suivre l'évolution d'une réaction inflammatoire au fil du temps."
            ),
            "en" to LocalizedText(
                name = "Erythrocyte Sedimentation Rate (ESR)",
                vulgarization = "Indirect indicator of red blood cell settling speed during inflammatory states.",
                recommendation = "Used to monitor the course and resolution of inflammatory conditions."
            ),
            "ar" to LocalizedText(
                name = "سرعة التثفل (VS)",
                vulgarization = "مؤشر غير مباشر لسرعة ترسب خلايا الدم الحمراء عند وجود التهاب.",
                recommendation = "يُستخدم لمتابعة تطور الحالات الالتهابية ومدى استجابتها للعلاج."
            )
        ),
        "tsh" to mapOf(
            "fr" to LocalizedText(
                name = "TSH (Thyréostimuline)",
                vulgarization = "Hormone hypophysaire régulant le fonctionnement de la glande thyroïde (métabolisme global).",
                recommendation = "Un taux équilibré garantit le bon niveau d'énergie, de poids et de rythme cardiaque."
            ),
            "en" to LocalizedText(
                name = "TSH (Thyroid-Stimulating Hormone)",
                vulgarization = "Pituitary hormone regulating the activity of your thyroid gland (overall metabolism).",
                recommendation = "A balanced level supports healthy energy, weight stability, and heart rate."
            ),
            "ar" to LocalizedText(
                name = "الهرمون المنبه للغدة الدرقية (TSH)",
                vulgarization = "هرمون نخامي ينظم وظيفة الغدة الدرقية ويتحكم في عملية التمثيل الغذائي (الأيض).",
                recommendation = "المستوى المتوازن يضمن الحفاظ على طاقة الجسم والوزن وانتظام ضربات القلب."
            )
        ),
        "vitamine_d" to mapOf(
            "fr" to LocalizedText(
                name = "Vitamine D (25-OH)",
                vulgarization = "Vitamine capitale pour l'absorption du calcium, l'immunité et la santé musculaire.",
                recommendation = "Exposition solaire modérée et supplémentation recommandée en période hivernale."
            ),
            "en" to LocalizedText(
                name = "Vitamin D (25-OH)",
                vulgarization = "Crucial vitamin for calcium absorption, bone strength, immunity, and muscle tone.",
                recommendation = "Moderate sun exposure and winter supplementation are commonly recommended."
            ),
            "ar" to LocalizedText(
                name = "فيتامين د (25-OH)",
                vulgarization = "فيتامين أساسي لامتصاص الكالسيوم وتقوية العظام والمناعة وصحة العضلات.",
                recommendation = "يُوصى بالتعرض المعتدل لأشعة الشمس واستشارة الطبيب بشأن المكملات شتاءً."
            )
        ),
        "vitamine_b12" to mapOf(
            "fr" to LocalizedText(
                name = "Vitamine B12",
                vulgarization = "Vitamine essentielle à la formation des globules rouges et à la protection des neurones.",
                recommendation = "Primordiale pour prévenir l'anémie mégaloblastique et la fatigue nerveuse."
            ),
            "en" to LocalizedText(
                name = "Vitamin B12",
                vulgarization = "Essential vitamin for red blood cell production and protective nerve function.",
                recommendation = "Key to preventing megaloblastic anemia and chronic neurological fatigue."
            ),
            "ar" to LocalizedText(
                name = "فيتامين ب12",
                vulgarization = "فيتامين حيوي لإنتاج خلايا الدم الحمراء وحماية الخلايا والألياف العصبية.",
                recommendation = "ضروري للوقاية من فقر الدم كبير الكريات والإرهاق العصبي."
            )
        )
    )

    // Détection de clé canonique à partir de n'importe quel nom (FR, EN, AR ou mot-clé)
    fun findCanonicalKey(biomarkerName: String): String? {
        val lower = biomarkerName.lowercase().trim()
        return when {
            lower.contains("hémoglobine glyquée") || lower.contains("hemoglobine glyquee") || lower.contains("hba1c") || lower.contains("glycated hemoglobin") || lower.contains("السكر التراكمي") -> "hba1c"
            lower.contains("hémoglobine") || lower.contains("hemoglobine") || lower.contains("hemoglobin") || lower.contains("hb") || lower.contains("الهيموغلوبين") -> "hemoglobine"
            lower.contains("hématie") || lower.contains("hematie") || lower.contains("globules rouges") || lower.contains("erythrocyte") || lower.contains("rbc") || lower.contains("كريات الدم الحمراء") -> "hematies"
            lower.contains("hématocrite") || lower.contains("hematocrite") || lower.contains("hematocrit") || lower.contains("ht") || lower.contains("الهيماتوكريت") -> "hematocrite"
            lower.contains("vgm") || lower.contains("mcv") || lower.contains("volume globulaire") || lower.contains("الحجم الكروي") -> "vgm"
            lower.contains("tcmh") || lower.contains("mch") || lower.contains("teneur corpusculaire") || lower.contains("هيموغلوبين الكرية") -> "tcmh"
            lower.contains("ccmh") || lower.contains("mchc") || lower.contains("concentration corpusculaire") || lower.contains("التركيز الوسطي") -> "ccmh"
            lower.contains("leucocyte") || lower.contains("globules blancs") || lower.contains("wbc") || lower.contains("white blood") || lower.contains("كريات الدم البيضاء") -> "leucocytes"
            lower.contains("neutrophil") || lower.contains("neutrophile") || lower.contains("الخلايا الحبيبية") -> "neutrophiles"
            lower.contains("lymphocyte") || lower.contains("الخلايا اللمفاوية") -> "lymphocytes"
            lower.contains("monocyte") || lower.contains("الخلايا الوحيدة") -> "monocytes"
            lower.contains("éosinophile") || lower.contains("eosinophile") || lower.contains("eosinophil") || lower.contains("الخلايا الحمضية") -> "eosinophiles"
            lower.contains("basophile") || lower.contains("basophil") || lower.contains("الخلايا القاعدية") -> "basophiles"
            lower.contains("plaquette") || lower.contains("thrombocyte") || lower.contains("platelet") || lower.contains("plt") || lower.contains("الصفائح الدموية") -> "plaquettes"
            lower.contains("glycémie") || lower.contains("glycemie") || lower.contains("glucose") || lower.contains("fasting blood glucose") || lower.contains("سكر الدم") -> "glycemie"
            lower.contains("ldl") || lower.contains("الكوليسترول الضار") -> "cholesterol_ldl"
            lower.contains("hdl") || lower.contains("الكوليسترول الجيد") -> "cholesterol_hdl"
            lower.contains("cholestérol total") || lower.contains("cholesterol total") || lower.contains("total cholesterol") || lower.contains("الكوليسترول الكلي") -> "cholesterol_total"
            lower.contains("triglycéride") || lower.contains("triglyceride") || lower.contains("tg") || lower.contains("الدهون الثلاثية") -> "triglycerides"
            lower.contains("dfg") || lower.contains("egfr") || lower.contains("clairance") || lower.contains("filtration glomérulaire") || lower.contains("الترشيح الكبيبي") -> "dfg"
            lower.contains("créatinine") || lower.contains("creatinine") || lower.contains("creat") || lower.contains("الكرياتينين") -> "creatinine"
            lower.contains("urée") || lower.contains("uree") || lower.contains("bun") || lower.contains("urea") || lower.contains("اليوريا") -> "uree"
            lower.contains("acide urique") || lower.contains("uric acid") || lower.contains("حمض اليوريك") -> "acide_urique"
            lower.contains("alat") || lower.contains("tgp") || lower.contains("alt") -> "alat"
            lower.contains("asat") || lower.contains("tgo") || lower.contains("ast") -> "asat"
            lower.contains("gamma gt") || lower.contains("gamma-gt") || lower.contains("ggt") || lower.contains("غاما جي تي") -> "ggt"
            lower.contains("phosphatases alcalines") || lower.contains("pal") || lower.contains("alp") || lower.contains("alkaline phosphatase") || lower.contains("الفوسفاتاز القلوي") -> "pal"
            lower.contains("bilirubine") || lower.contains("bilirubin") || lower.contains("البيليروبين") -> "bilirubine"
            lower.contains("sodium") || lower.contains("natrémie") || lower.contains("natremie") || lower.contains("الصوديوم") -> "sodium"
            lower.contains("potassium") || lower.contains("kaliémie") || lower.contains("kaliemie") || lower.contains("البوتاسيوم") -> "potassium"
            lower.contains("chlore") || lower.contains("chloride") || lower.contains("chlorémie") || lower.contains("الكلوريد") -> "chlore"
            lower.contains("calcium") || lower.contains("calcémie") || lower.contains("calcemie") || lower.contains("الكالسيوم") -> "calcium"
            lower.contains("ferritine") || lower.contains("ferritin") || lower.contains("الفيريتين") -> "ferritine"
            lower.contains("fer sérique") || lower.contains("fer serique") || lower.contains("serum iron") || lower.contains("الحديد في مصل الدم") -> "fer_serique"
            lower.contains("crp") || lower.contains("protéine c-réactive") || lower.contains("c-reactive") || lower.contains("البروتين التفاعلي") -> "crp"
            lower.contains("vitesse de sédimentation") || lower.contains("vitesse de sedimentation") || lower.contains("esr") || lower.contains("vs") || lower.contains("سرعة التثفل") -> "vs"
            lower.contains("tsh") || lower.contains("thyréostimuline") || lower.contains("thyreostimuline") || lower.contains("الهرمون المنبه للغدة") -> "tsh"
            lower.contains("vitamine d") || lower.contains("vitamin d") || lower.contains("25-oh") || lower.contains("فيتامين د") -> "vitamine_d"
            lower.contains("vitamine b12") || lower.contains("vitamin b12") || lower.contains("b12") || lower.contains("cobalamine") || lower.contains("فيتامين ب12") -> "vitamine_b12"
            else -> null
        }
    }

    /**
     * Localise un biomarqueur individuel selon la langue demandée ("fr", "en", "ar").
     */
    fun localizeBiomarker(biomarker: Biomarker, targetLanguage: String): Biomarker {
        val lang = targetLanguage.lowercase().take(2).ifBlank { "fr" }
        val key = findCanonicalKey(biomarker.name)
        val localizedMap = key?.let { standardBiomarkersMap[it] }

        if (localizedMap != null) {
            val targetText = localizedMap[lang] ?: localizedMap["fr"] ?: localizedMap["en"]!!
            return biomarker.copy(
                name = targetText.name,
                vulgarization = targetText.vulgarization,
                recommendation = targetText.recommendation
            )
        }

        // Si le biomarqueur n'est pas dans le dictionnaire standard (découverte dynamique)
        val defaultVulg = when (lang) {
            "en" -> "Biological parameter extracted from your medical lab report."
            "ar" -> "مؤشر بيولوجي تم استخراجه من تقرير التحاليل الطبية الخاص بك."
            else -> "Paramètre biologique extrait de votre compte-rendu de laboratoire."
        }
        val defaultRec = when (lang) {
            "en" -> "To be discussed during a medical consultation with your doctor."
            "ar" -> "يُرجى مناقشته خلال استشارة طبية مع طبيبك المعالج."
            else -> "À évaluer en consultation médicale avec votre praticien."
        }

        return biomarker.copy(
            vulgarization = if (isDefaultFallback(biomarker.vulgarization) || biomarker.vulgarization.isBlank()) defaultVulg else biomarker.vulgarization,
            recommendation = if (isDefaultRecommendation(biomarker.recommendation) || biomarker.recommendation.isBlank()) defaultRec else biomarker.recommendation
        )
    }

    /**
     * Localise l'ensemble d'un bilan (titre, synthèse globale et tous les biomarqueurs).
     */
    fun localizeReport(report: LabReport, targetLanguage: String): LabReport {
        val lang = targetLanguage.lowercase().take(2).ifBlank { "fr" }
        val localizedBiomarkers = report.biomarkers.map { localizeBiomarker(it, lang) }
        val localizedTitle = localizeTitle(report.title, lang)
        val localizedSummary = localizeDoctorSummary(
            originalSummary = report.doctorSummary,
            totalBiomarkers = localizedBiomarkers.size,
            abnormalCount = localizedBiomarkers.count { it.isAbnormal },
            targetLanguage = lang
        )

        return report.copy(
            title = localizedTitle,
            biomarkers = localizedBiomarkers,
            doctorSummary = localizedSummary
        )
    }

    fun localizeDoctorSummary(
        originalSummary: String,
        totalBiomarkers: Int,
        abnormalCount: Int,
        targetLanguage: String
    ): String {
        val lang = targetLanguage.lowercase().take(2).ifBlank { "fr" }
        
        // Si la synthèse originale est vide ou est un résumé standard généré par fallback
        val isStandardSummary = originalSummary.isBlank() ||
                originalSummary.contains("Bilan comprenant") ||
                originalSummary.contains("Analyse du document") ||
                originalSummary.contains("Report containing") ||
                originalSummary.contains("يتضمن التقرير")

        if (isStandardSummary) {
            return when (lang) {
                "en" -> {
                    if (totalBiomarkers == 0) {
                        "Document analysis complete. For optimal precision on handwritten reports, you can provide a Gemini API key."
                    } else if (abnormalCount > 0) {
                        "Report containing $totalBiomarkers analyzed parameters. $abnormalCount biomarker(s) are outside the laboratory reference ranges and require medical advice."
                    } else {
                        "Report containing $totalBiomarkers analyzed parameters. All measured biological markers are within standard laboratory reference ranges."
                    }
                }
                "ar" -> {
                    if (totalBiomarkers == 0) {
                        "اكتمل تحليل المستند. للحصول على أفضل دقة في التقارير المكتوبة بخط اليد، يمكنك إضافة مفتاح Gemini API."
                    } else if (abnormalCount > 0) {
                        "يتضمن التقرير تحليل $totalBiomarkers مؤشراً. يوجد $abnormalCount مؤشر(ات) خارج المعدلات المرجعية للمختبر وتتطلب استشارة طبية."
                    } else {
                        "يتضمن التقرير تحليل $totalBiomarkers مؤشراً. جميع المؤشرات البيولوجية المقاسة تقع ضمن المعدلات الطبيعية المرجعية للمختبر."
                    }
                }
                else -> {
                    if (totalBiomarkers == 0) {
                        "Analyse du document effectuée. Pour une précision optimale sur les bilans manuscrits ou atypiques, vous pouvez également renseigner une clé Gemini API."
                    } else if (abnormalCount > 0) {
                        "Bilan comprenant $totalBiomarkers paramètres analysés. $abnormalCount biomarqueur(s) se situent en dehors des valeurs de référence du laboratoire et nécessitent un avis médical."
                    } else {
                        "Bilan comprenant $totalBiomarkers paramètres analysés. L'ensemble des marqueurs biologiques mesurés est dans les normes de référence du laboratoire."
                    }
                }
            }
        }

        // Si c'est un résumé textuel personnalisé provenant de Gemini
        return originalSummary
    }

    private fun localizeTitle(originalTitle: String, targetLanguage: String): String {
        val lower = originalTitle.lowercase()
        val lang = targetLanguage.lowercase().take(2).ifBlank { "fr" }
        return when {
            lower.contains("hématologique") || lower.contains("nfs") || lower.contains("hematolog") || lower.contains("دموي") -> when (lang) {
                "en" -> "Hematology & CBC Panel"
                "ar" -> "تحليل الدم الشامل (CBC)"
                else -> "Bilan Hématologique & NFS"
            }
            lower.contains("biochimique") || lower.contains("glycémie") || lower.contains("biochem") || lower.contains("كيميائي") -> when (lang) {
                "en" -> "Biochemical & Blood Panel"
                "ar" -> "التحليل الكيميائي الحيوي"
                else -> "Bilan Biochimique & Sanguin"
            }
            lower.contains("lipidique") || lower.contains("cholestérol") || lower.contains("lipid") || lower.contains("دهون") -> when (lang) {
                "en" -> "Lipid & Cardiovascular Panel"
                "ar" -> "فحص الدهون وصحة القلب"
                else -> "Bilan Lipidique & Cardiovasculaire"
            }
            lower.contains("thyro") || lower.contains("tsh") || lower.contains("درقية") -> when (lang) {
                "en" -> "Thyroid & Hormone Panel"
                "ar" -> "فحص الغدة الدرقية والهرمونات"
                else -> "Bilan Thyroïdien & Hormonal"
            }
            lower.contains("rénal") || lower.contains("créatinine") || lower.contains("renal") || lower.contains("كلو") -> when (lang) {
                "en" -> "Renal & Electrolyte Panel"
                "ar" -> "فحص وظائف الكلى والكهارل"
                else -> "Bilan Rénal & Ionique"
            }
            else -> when (lang) {
                "en" -> "Biological Lab Report"
                "ar" -> "تقرير التحاليل البيولوجية"
                else -> originalTitle.ifBlank { "Bilan d'Analyses Biologiques" }
            }
        }
    }

    private fun isDefaultFallback(text: String): Boolean {
        return text.contains("Paramètre biologique extrait") ||
                text.contains("Biological parameter extracted") ||
                text.contains("مؤشر بيولوجي تم استخراجه") ||
                text.contains("Mesure biologique analysée")
    }

    private fun isDefaultRecommendation(text: String): Boolean {
        return text.contains("À évaluer en consultation") ||
                text.contains("To be discussed during") ||
                text.contains("يُرجى مناقشته خلال استشارة")
    }

    fun getBiomarkerDefinition(key: String, language: String): LocalizedText? {
        val lang = language.lowercase().take(2).ifBlank { "fr" }
        val map = standardBiomarkersMap[key] ?: return null
        return map[lang] ?: map["fr"] ?: map["en"]
    }
}
