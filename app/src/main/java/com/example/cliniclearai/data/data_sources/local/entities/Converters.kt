package com.example.cliniclearai.data.data_sources.local.entities

import androidx.room.TypeConverter
import com.example.cliniclearai.domain.models.Biomarker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBiomarkerList(biomarkers: List<Biomarker>?): String {
        if (biomarkers == null) return "[]"
        return gson.toJson(biomarkers)
    }

    @TypeConverter
    fun toBiomarkerList(json: String?): List<Biomarker> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<Biomarker>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String {
        if (strings == null) return "[]"
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
