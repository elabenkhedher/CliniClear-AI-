package com.example.cliniclearai.data.data_sources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drugs")
data class DrugEntity(
    @PrimaryKey val id: String,
    val brandName: String,
    val genericName: String,
    val activeIngredients: List<String>,
    val dosage: String,
    val frequency: String,
    val instructions: String,
    val purpose: String,
    val warnings: String,
    val hasActiveRecall: Boolean,
    val isPrescription: Boolean
)
