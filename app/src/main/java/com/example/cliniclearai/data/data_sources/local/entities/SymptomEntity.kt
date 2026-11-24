package com.example.cliniclearai.data.data_sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey val id: String,
    val bodyPart: String,
    val intensity: Int,
    val description: String,
    val timestamp: Long,
    val associatedFactors: String
)
