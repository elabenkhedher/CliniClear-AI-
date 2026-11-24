package com.example.cliniclearai.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cliniclearai.common.Resource
import com.example.cliniclearai.data.repositories.MedicalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CheckDrugRecallsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicalRepository: MedicalRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = medicalRepository.checkDrugRecallsOnline()
            when (result) {
                is Resource.Success -> Result.success()
                is Resource.Error -> {
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is Resource.Loading -> Result.retry()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
