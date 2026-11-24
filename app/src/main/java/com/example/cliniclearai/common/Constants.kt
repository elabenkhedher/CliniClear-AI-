package com.example.cliniclearai.common

import com.example.cliniclearai.BuildConfig

object Constants {
    // OpenFDA API Endpoints
    const val OPEN_FDA_BASE_URL = "https://api.fda.gov/"
    const val DRUG_LABEL_ENDPOINT = "drug/label.json"
    const val DRUG_ENFORCEMENT_ENDPOINT = "drug/enforcement.json"
    const val DEFAULT_API_LIMIT = 10

    // Google Gemini API
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    const val DEFAULT_GEMINI_MODEL = "gemini-1.5-flash"
    // Clé Gemini optionnelle injectée de manière sécurisée via BuildConfig (local.properties)
    val DEFAULT_GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY

    // Database & DataStore (Chiffrement SQLCipher au repos)
    const val DATABASE_NAME = "cliniclear_encrypted.db"
    const val LEGACY_DATABASE_NAME = "cliniclear_db"
    const val PREFERENCES_DATASTORE_NAME = "cliniclear_preferences"

    // Navigation Arguments & Routes
    const val PARAM_REPORT_ID = "reportId"

    // WorkManager
    const val CHECK_DRUG_RECALLS_WORK_NAME = "check_drug_recalls_work"
    const val CHECK_DRUG_RECALLS_TAG = "drug_recalls_tag"

    // Network Timeouts
    const val NETWORK_TIMEOUT_SECONDS = 30L
}
