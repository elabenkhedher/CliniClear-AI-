package com.example.cliniclearai.data.data_sources.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cliniclearai.data.data_sources.local.entities.Converters
import com.example.cliniclearai.data.data_sources.local.entities.DrugEntity
import com.example.cliniclearai.data.data_sources.local.entities.LabReportEntity
import com.example.cliniclearai.data.data_sources.local.entities.SymptomEntity
import com.example.cliniclearai.domain.models.Biomarker
import com.example.cliniclearai.domain.models.BiomarkerStatus

@Database(
    entities = [
        LabReportEntity::class,
        DrugEntity::class,
        SymptomEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CliniClearDatabase : RoomDatabase() {

    abstract fun medicalDao(): MedicalDao

    companion object {
        fun getInitialLabReports(): List<LabReportEntity> = emptyList()
        fun getInitialDrugs(): List<DrugEntity> = emptyList()
    }
}
