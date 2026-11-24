# 🩺 CliniClear AI — Plan d'Architecture & Spécifications

## 1. Présentation du Projet
**CliniClear AI** est une application Android moderne (Kotlin, Jetpack Compose, Material 3) fonctionnant en mode **offline-first**. Elle aide les patients à comprendre leurs bilans médicaux, à détecter les interactions médicamenteuses (via l'API gratuite **OpenFDA**) et à préparer une fiche de synthèse clinique (**Brief Médecin** au format SOAP) avant une consultation.

> **Objectif architectural :** Respecter strictement la **Clean Architecture** en 3 couches (**UI**, **Domain**, **Data**) avec un **flux de données unidirectionnel (UDF)**, en suivant exactement l'organisation des packages du projet de référence `Crypto` et les bonnes pratiques Android Jetpack.

---

## 2. Stack Technologique
- **Langage & UI :** Kotlin, Jetpack Compose, Material 3, Navigation Compose
- **Asynchronisme :** Kotlin Coroutines, `Flow`, `StateFlow`
- **Injection de dépendances :** Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **Réseau :** Retrofit2 + Gson Converter (API OpenFDA + API IA en phase finale)
- **Persistance locale :**
  - **Room** : Base de données relationnelle (**Single Source of Truth**)
  - **Preferences DataStore** : Stockage léger des préférences utilisateur
- **Tâches critiques en arrière-plan :** WorkManager
- **IA & OCR (Phase 5) :** Google ML Kit (Text Recognition) + Gemini API

---

## 3. Arborescence Obligatoire des Packages
Respecter exactement cette structure sous `com.example.cliniclear/` :

```text
com.example.cliniclear/
├── CliniClearApplication.kt                # @HiltAndroidApp
├── common/
│   ├── Constants.kt                        # Base URLs, DB name, nav arguments
│   └── Resource.kt                         # Sealed class (Success, Error, Loading)
├── data/
│   ├── data_sources/
│   │   ├── local/
│   │   │   ├── CliniClearDatabase.kt       # RoomDatabase
│   │   │   ├── MedicalDao.kt               # Requêtes Flow + suspend CRUD
│   │   │   ├── UserPreferencesDataSource.kt# DataStore (unités, filtres, langue)
│   │   │   └── entities/                   # LabReportEntity, DrugEntity, SymptomEntity
│   │   └── remote/
│   │       ├── OpenFdaApi.kt               # Interface Retrofit (OpenFDA)
│   │       ├── AiAnalysisApi.kt            # Interface Retrofit (Vulgarisation IA - Phase 5)
│   │       └── dto/                        # DrugLabelDto, DrugRecallDto, AiReportDto + mappers
│   ├── repositories/
│   │   ├── MedicalRepository.kt            # Interface
│   │   ├── MedicalRepositoryImpl.kt        # Implémentation SSOT + Mutex cache
│   │   └── UserPreferencesRepository.kt    # Interface + Implémentation DataStore
│   └── workers/
│       └── CheckDrugRecallsWorker.kt       # CoroutineWorker (WorkManager)
├── di/
│   └── AppModule.kt                        # Providers Hilt (Retrofit, Room, DataStore, Repos, Dispatchers)
├── domain/
│   ├── models/                             # Modèles métier immuables (LabReport, Biomarker, Drug, SymptomEntry, DoctorBrief)
│   └── use_cases/
│       ├── GetReportsUseCase.kt            # Combine rapports + préférences
│       ├── GetReportDetailUseCase.kt       # Récupération détail (avec cache Mutex)
│       ├── CheckDrugInteractionsUseCase.kt # Détection de conflits d'ingrédients actifs
│       └── GenerateDoctorBriefUseCase.kt   # Agrégation multi-repositories -> Fiche SOAP
└── ui/
    ├── MainActivity.kt                     # @AndroidEntryPoint + NavHost
    ├── Screen.kt                           # Sealed class des routes de navigation
    ├── report_list/
    │   ├── state_holder/
    │   │   ├── ReportListState.kt          # État UI immuable + errorMessages
    │   │   └── ReportListViewModel.kt      # StateFlow + combine + stateIn
    │   └── ui_elements/
    │       ├── ReportListScreen.kt         # Dashboard, recherche, filtres, Snackbar
    │       └── ReportListItem.kt           # Carte de bilan / médicament
    ├── report_detail/
    │   ├── state_holder/
    │   │   ├── ReportDetailState.kt
    │   │   └── ReportDetailViewModel.kt
    │   └── ui_elements/
    │       ├── ReportDetailScreen.kt       # Jauges biomarqueurs + alertes OpenFDA
    │       └── BiomarkerGaugeCard.kt       # Carte dépliable (logique UI locale)
    ├── body_map/
    │   ├── state_holder/
    │   │   ├── BodyMapState.kt
    │   │   └── BodyMapViewModel.kt
    │   └── ui_elements/
    │       └── BodyMapScreen.kt            # Sélection zone corporelle + intensité 1-10
    ├── doctor_brief/
    │   ├── state_holder/
    │   │   ├── DoctorBriefState.kt
    │   │   └── DoctorBriefViewModel.kt
    │   └── ui_elements/
    │       └── DoctorBriefScreen.kt        # Fiche clinique SOAP + partage
    └── theme/                              # Color.kt, Theme.kt, Type.kt (Material 3)
```

---

## 4. Règles d'Architecture Strictes (Non Négociables)

### A. Couche Data (`data/`)
1. **Single Source of Truth (SSOT) & Hors-ligne :**
   - Aucune autre couche n'accède directement aux Data Sources : tout passe par les **Repositories**.
   - Les repositories récupèrent les données depuis Retrofit, mettent à jour **Room**, et exposent un `Flow` directement depuis Room.
   - En cas d'erreur réseau, l'application continue d'afficher les données locales sans planter.
2. **Séparation Room vs DataStore :**
   - **Room** : Données volumineuses, structurées et requêtables (bilans, médicaments, symptômes).
   - **DataStore** : Préférences simples (ex. `showOnlyAlerts`, `preferredUnits`, `explanationLanguage`).
3. **Business Models (Mapping DTO) :**
   - Ne jamais exposer les classes `dto/` ou `entities/` en dehors de la couche `data/`. Toujours mapper vers les modèles propres de `domain/models/`.
4. **Cache mémoire thread-safe (`Mutex`) :**
   - Utiliser un `Mutex` (`withLock`) dans `MedicalRepositoryImpl` pour protéger le cache mémoire des détails récemment consultés.
5. **Opérations critiques (`WorkManager`) :**
   - Utiliser `CheckDrugRecallsWorker` pour les vérifications de rappels de médicaments devant survivre à la fermeture du processus.
6. **Main-Safety :**
   - Toutes les fonctions `suspend` des Data Sources et Repositories doivent être *main-safe* (injecter `CoroutineDispatcher` et utiliser `withContext(ioDispatcher)` si nécessaire).

### B. Couche Domain (`domain/`)
1. **Stateless & Responsabilité Unique :**
   - Les Use Cases ne doivent contenir **aucune donnée mutable**.
2. **Opérateur `invoke()` :**
   - Chaque Use Case expose sa logique via `operator fun invoke(...)`.
3. **Combinaison & Threading :**
   - Les Use Cases combinent plusieurs repositories lorsque nécessaire (ex. `GenerateDoctorBriefUseCase`).
   - Les calculs lourds (croisement de molécules, agrégation SOAP) s'exécutent sur `Dispatchers.Default` via `withContext`.

### C. Couche UI (`ui/`)
1. **Unidirectional Data Flow (UDF) & État Immuable :**
   - Chaque écran observe un seul `StateFlow<UiState>` immuable (data class avec `val`).
   - Utiliser `combine(...)` + `.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = ...)` dans les `ViewModel`.
2. **Logique UI vs Logique Métier :**
   - **Logique purement UI** (ex. déplier/replier l'explication d'un biomarqueur, ouvrir un menu) $\rightarrow$ gérée localement dans le Composable avec `remember { mutableStateOf(false) }`.
   - **Logique métier** (ex. rafraîchir, ajouter un symptôme, générer le brief, sauvegarder) $\rightarrow$ déléguée au `ViewModel`.
3. **Gestion des erreurs (ViewModel Events) :**
   - Modéliser les erreurs dans l'état UI (`val errorMessages: List<UiError> = emptyList()`).
   - Dans le Composable, utiliser `LaunchedEffect` pour afficher un `Snackbar` avec une action **Réessayer**.
   - Une fois affiché ou fermé, notifier le `ViewModel` via `viewModel.errorShown(errorId)` pour purger le message de l'état.
4. **Feedback visuel :**
   - Toujours afficher un indicateur de chargement (`isLoading`) pendant les opérations asynchrones.

---

## 5. APIs & Données de Démarrage
- **OpenFDA Drug Label API (Gratuite, sans clé) :**
  - `GET https://api.fda.gov/drug/label.json?search=openfda.brand_name:{query}&limit=10`
- **OpenFDA Drug Enforcement / Recalls (Gratuite, sans clé) :**
  - `GET https://api.fda.gov/drug/enforcement.json?search=status:Ongoing&limit=10`
- **Données initiales (Seed) :** Pré-remplir Room au premier lancement avec 2 bilans sanguins d'exemple (ex. *Bilan Sanguin Annuel*, *Bilan Lipidique*) afin que l'application soit immédiatement testable et démonstrative.

---

## 6. Feuille de Route d'Implémentation (Phases)
Exécuter **une phase à la fois** et vérifier que le build Gradle réussit avant de passer à la suivante :

- [ ] **Phase 1 — Setup & Fondation :** Configuration `build.gradle.kts` (Hilt, Room, Retrofit, DataStore, WorkManager, Compose Navigation), `CliniClearApplication`, `common/Constants.kt`, `common/Resource.kt`, `di/AppModule.kt`, thème Material 3.
- [ ] **Phase 2 — Couche Data :** `OpenFdaApi` + DTOs/mappers, `CliniClearDatabase` + `MedicalDao` + Entities, `UserPreferencesDataSource`, `MedicalRepositoryImpl` (SSOT + `Mutex`), `CheckDrugRecallsWorker`.
- [ ] **Phase 3 — Couche Domain :** Modèles métier immuables (`LabReport`, `Biomarker`, `Drug`, `SymptomEntry`, `DoctorBrief`) et Use Cases (`GetReportsUseCase`, `GetReportDetailUseCase`, `CheckDrugInteractionsUseCase`, `GenerateDoctorBriefUseCase`).
- [ ] **Phase 4 — Couche UI & Navigation :** `Screen.kt`, `MainActivity.kt`, écrans `report_list`, `report_detail`, `body_map`, `doctor_brief` avec leurs `State`, `ViewModel` (`StateFlow` + `combine` + `stateIn`), Composables et gestion `Snackbar` des erreurs.
- [ ] **Phase 5 — Intelligence Artificielle & OCR (Extension) :** Intégration de ML Kit Text Recognition (capture photo/galerie) et appel à l'API Gemini pour analyser un vrai bilan scanné et générer le résumé SOAP dynamique.