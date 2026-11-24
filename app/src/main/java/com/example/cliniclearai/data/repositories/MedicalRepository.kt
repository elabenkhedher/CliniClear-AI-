package com.example.cliniclearai.data.repositories

import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.domain.models.Drug
import com.example.cliniclearai.domain.models.DrugRecall
import com.example.cliniclearai.domain.models.LabReport
import com.example.cliniclearai.domain.models.SymptomEntry
import kotlinx.coroutines.flow.Flow

interface MedicalRepository {

    // --- Initialisation / Seeding & Nettoyage ---
    suspend fun initializeSeedDataIfEmpty()
    suspend fun clearTestData()
    suspend fun clearAllData()

    // --- Lab Reports (SSOT: Room via Flow) ---
    fun getLabReports(): Flow<List<LabReport>>
    fun getLabReportById(id: String): Flow<LabReport?>
    suspend fun getLabReportDetail(id: String): Resource<LabReport>
    suspend fun saveLabReport(report: LabReport)
    suspend fun deleteLabReport(id: String)
    suspend fun analyzeOcrTextAndSave(ocrText: String, apiKey: String = "", language: String = ""): Resource<LabReport>
    suspend fun updateReportLanguage(reportId: String, language: String): Resource<LabReport>

    // --- Drugs ---
    fun getDrugs(): Flow<List<Drug>>
    fun getDrugById(id: String): Flow<Drug?>
    suspend fun saveDrug(drug: Drug)
    suspend fun deleteDrug(id: String)
    suspend fun updateDrugRecallStatus(drugId: String, hasRecall: Boolean)
    suspend fun analyzePrescriptionOcrTextAndSave(ocrText: String, apiKey: String = "", language: String = ""): Resource<List<Drug>>

    // --- Remote OpenFDA ---
    suspend fun searchDrugsOnline(query: String): Resource<List<Drug>>
    suspend fun checkDrugRecallsOnline(): Resource<List<DrugRecall>>

    // --- Symptoms ---
    fun getSymptoms(): Flow<List<SymptomEntry>>
    suspend fun saveSymptom(symptom: SymptomEntry)
    suspend fun deleteSymptom(id: String)
}
