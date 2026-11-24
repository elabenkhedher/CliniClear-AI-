package com.example.cliniclearai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.data.workers.CheckDrugRecallsWorker
import com.example.cliniclearai.di.IoDispatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CliniClearApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var medicalRepository: MedicalRepository

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + ioDispatcher).launch {
            medicalRepository.initializeSeedDataIfEmpty()
        }

        // Planification périodique de la vérification des rappels sanitaires OpenFDA (toutes les 24h)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val recallWork = PeriodicWorkRequestBuilder<CheckDrugRecallsWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(Constants.CHECK_DRUG_RECALLS_TAG)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.CHECK_DRUG_RECALLS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            recallWork
        )
    }
}
