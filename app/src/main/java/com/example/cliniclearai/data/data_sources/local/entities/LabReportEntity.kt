package com.example.cliniclearai.data.data_sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cliniclearai.domain.models.Biomarker

@Entity(tableName = "lab_reports")
data class LabReportEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val laboratory: String,
    val biomarkers: List<Biomarker>,
    val doctorSummary: String,
    val hasAlerts: Boolean
)
