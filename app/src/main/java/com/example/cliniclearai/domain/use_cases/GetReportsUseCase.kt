package com.example.cliniclearai.domain.use_cases

import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.data.repositories.UserPreferencesRepository
import com.example.cliniclearai.domain.models.LabReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Cas d'utilisation : Récupération des rapports de laboratoire combinés aux préférences utilisateur.
 *
 * Points clés du cours pour le jury :
 * - SSOT (Single Source of Truth) : Les données proviennent exclusivement du Repository (Room via Flow).
 * - Immutabilité & UDF : Aucune variable mutable dans le UseCase, le flux émet des listes immuables.
 */
class GetReportsUseCase @Inject constructor(
    private val medicalRepository: MedicalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(query: String = ""): Flow<List<LabReport>> {
        return combine(
            medicalRepository.getLabReports(),
            userPreferencesRepository.getUserPreferences()
        ) { reports, preferences ->
            reports
                .filter { report ->
                    if (preferences.showOnlyAlerts) report.hasAlerts else true
                }
                .filter { report ->
                    if (query.isBlank()) true
                    else report.title.contains(query, ignoreCase = true) ||
                            report.laboratory.contains(query, ignoreCase = true) ||
                            report.biomarkers.any { it.name.contains(query, ignoreCase = true) }
                }
        }
    }
}
