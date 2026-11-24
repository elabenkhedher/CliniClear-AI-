package com.example.cliniclearai.data.data_sources.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cliniclearai.data.data_sources.local.entities.DrugEntity
import com.example.cliniclearai.data.data_sources.local.entities.LabReportEntity
import com.example.cliniclearai.data.data_sources.local.entities.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {

    // --- Lab Reports ---
    @Query("SELECT * FROM lab_reports ORDER BY date DESC")
    fun getAllLabReports(): Flow<List<LabReportEntity>>

    @Query("SELECT * FROM lab_reports WHERE id = :id")
    fun getLabReportById(id: String): Flow<LabReportEntity?>

    @Query("SELECT * FROM lab_reports WHERE id = :id")
    suspend fun getLabReportByIdDirect(id: String): LabReportEntity?

    @Query("SELECT COUNT(*) FROM lab_reports")
    suspend fun getReportsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabReport(report: LabReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabReports(reports: List<LabReportEntity>)

    @Delete
    suspend fun deleteLabReport(report: LabReportEntity)

    @Query("DELETE FROM lab_reports WHERE id = :id")
    suspend fun deleteLabReportById(id: String)

    @Query("DELETE FROM lab_reports")
    suspend fun deleteAllLabReports()

    // --- Drugs ---
    @Query("SELECT * FROM drugs ORDER BY brandName ASC")
    fun getAllDrugs(): Flow<List<DrugEntity>>

    @Query("SELECT * FROM drugs WHERE id = :id")
    fun getDrugById(id: String): Flow<DrugEntity?>

    @Query("SELECT * FROM drugs WHERE id = :id")
    suspend fun getDrugByIdDirect(id: String): DrugEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrug(drug: DrugEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<DrugEntity>)

    @Update
    suspend fun updateDrug(drug: DrugEntity)

    @Query("UPDATE drugs SET hasActiveRecall = :hasRecall WHERE id = :id")
    suspend fun updateDrugRecallStatus(id: String, hasRecall: Boolean)

    @Delete
    suspend fun deleteDrug(drug: DrugEntity)

    @Query("DELETE FROM drugs WHERE id = :id")
    suspend fun deleteDrugById(id: String)

    @Query("DELETE FROM drugs")
    suspend fun deleteAllDrugs()

    // --- Symptoms ---
    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    fun getAllSymptoms(): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE id = :id")
    fun getSymptomById(id: String): Flow<SymptomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)

    @Query("DELETE FROM symptoms WHERE id = :id")
    suspend fun deleteSymptomById(id: String)

    @Query("DELETE FROM symptoms")
    suspend fun deleteAllSymptoms()
}
