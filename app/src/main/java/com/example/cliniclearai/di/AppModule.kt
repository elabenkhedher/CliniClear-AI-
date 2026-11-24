package com.example.cliniclearai.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.cliniclearai.common.Constants
import com.example.cliniclearai.data.data_sources.local.CliniClearDatabase
import com.example.cliniclearai.data.data_sources.local.DatabaseKeyManager
import com.example.cliniclearai.data.data_sources.local.MedicalDao
import com.example.cliniclearai.data.data_sources.local.OcrDataSource
import com.example.cliniclearai.data.data_sources.local.UserPreferencesDataSource
import com.example.cliniclearai.data.data_sources.remote.AiAnalysisApi
import com.example.cliniclearai.data.data_sources.remote.OpenFdaApi
import com.example.cliniclearai.data.repositories.MedicalRepository
import com.example.cliniclearai.data.repositories.MedicalRepositoryImpl
import com.example.cliniclearai.data.repositories.UserPreferencesRepository
import com.example.cliniclearai.data.repositories.UserPreferencesRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenFdaRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Dispatchers ---

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    // --- Network (Retrofit & OkHttp & Gson) ---

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @OpenFdaRetrofit
    fun provideOpenFdaRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.OPEN_FDA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenFdaApi(@OpenFdaRetrofit retrofit: Retrofit): OpenFdaApi {
        return retrofit.create(OpenFdaApi::class.java)
    }

    @Provides
    @Singleton
    @GeminiRetrofit
    fun provideGeminiRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAiAnalysisApi(@GeminiRetrofit retrofit: Retrofit): AiAnalysisApi {
        return retrofit.create(AiAnalysisApi::class.java)
    }

    // --- Local Database (Room avec SQLCipher + Android KeyStore) ---

    @Provides
    @Singleton
    fun provideDatabaseKeyManager(
        @ApplicationContext context: Context
    ): DatabaseKeyManager {
        return DatabaseKeyManager(context)
    }

    @Provides
    @Singleton
    fun provideCliniClearDatabase(
        @ApplicationContext context: Context,
        databaseKeyManager: DatabaseKeyManager
    ): CliniClearDatabase {
        // Chargement de la bibliothèque native SQLCipher
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            // Ignoré si déjà chargé par le système ou l'environnement
        }

        // Nettoyage sécurisé de l'ancienne base non chiffrée pour éviter tout conflit ou crash
        databaseKeyManager.cleanupLegacyUnencryptedDatabase(Constants.LEGACY_DATABASE_NAME)

        // Récupération de la clé 256 bits chiffrée par Android KeyStore
        val passphrase = databaseKeyManager.getOrCreatePassphrase()
        val openHelperFactory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CliniClearDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .openHelperFactory(openHelperFactory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMedicalDao(database: CliniClearDatabase): MedicalDao {
        return database.medicalDao()
    }

    // --- Local Storage (Preferences DataStore) ---

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile(Constants.PREFERENCES_DATASTORE_NAME) }
        )
    }

    // --- Repositories ---

    @Provides
    @Singleton
    fun provideMedicalRepository(
        medicalDao: MedicalDao,
        openFdaApi: OpenFdaApi,
        aiAnalysisApi: AiAnalysisApi,
        userPreferencesDataSource: UserPreferencesDataSource,
        gson: Gson,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MedicalRepository {
        return MedicalRepositoryImpl(medicalDao, openFdaApi, aiAnalysisApi, userPreferencesDataSource, gson, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataSource: UserPreferencesDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataSource, ioDispatcher)
    }
}
