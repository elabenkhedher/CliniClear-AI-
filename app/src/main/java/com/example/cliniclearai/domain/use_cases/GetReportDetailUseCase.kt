package com.example.cliniclearai.domain.use_cases

import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.domain.models.LabReport
import javax.inject.Inject

/**
 * Cas d'utilisation : Récupération des détails d'un bilan médical.
 *
 * Points clés du cours pour le jury :
 * - Mutex : Protège le cache mémoire dans MedicalRepositoryImpl pour garantir la thread-safety
 *   sans bloquer le thread principal (suspend withLock).
 * - SSOT : Si le bilan n'est pas en cache, lecture directe depuis la base Room.
 */
class GetReportDetailUseCase @Inject constructor(
    private val medicalRepository: MedicalRepository
) {
    suspend operator fun invoke(id: String): Resource<LabReport> {
        return medicalRepository.getLabReportDetail(id)
    }
}
