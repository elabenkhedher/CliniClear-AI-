package com.example.cliniclearai.data.repositories

import com.example.cliniclearai.data.data_sources.local.UserPreferencesDataSource
import com.example.cliniclearai.di.IoDispatcher
import com.example.cliniclearai.domain.models.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun setShowOnlyAlerts(showOnlyAlerts: Boolean)
    suspend fun setPreferredUnits(preferredUnits: String)
    suspend fun setExplanationLanguage(language: String)
    suspend fun setGeminiApiKey(apiKey: String)
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataSource.userPreferencesFlow
    }

    override suspend fun setShowOnlyAlerts(showOnlyAlerts: Boolean): Unit = withContext(ioDispatcher) {
        dataSource.updateShowOnlyAlerts(showOnlyAlerts)
    }

    override suspend fun setPreferredUnits(preferredUnits: String): Unit = withContext(ioDispatcher) {
        dataSource.updatePreferredUnits(preferredUnits)
    }

    override suspend fun setExplanationLanguage(language: String): Unit = withContext(ioDispatcher) {
        dataSource.updateExplanationLanguage(language)
    }

    override suspend fun setGeminiApiKey(apiKey: String): Unit = withContext(ioDispatcher) {
        dataSource.updateGeminiApiKey(apiKey)
    }
}
