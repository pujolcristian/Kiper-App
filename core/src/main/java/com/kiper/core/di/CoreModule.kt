package com.kiper.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.kiper.core.data.ApiService
import com.kiper.core.data.ConditionalLoggingInterceptor
import com.kiper.core.data.repository.AudioRepositoryImpl
import com.kiper.core.data.repository.CapsuleRepositoryImpl
import com.kiper.core.data.repository.UpdateRepositoryImpl
import com.kiper.core.data.source.local.AudioLocalDataSource
import com.kiper.core.data.source.local.dao.AudioRecordingDao
import com.kiper.core.data.source.local.dao.ScheduleDao
import com.kiper.core.data.source.local.db.AppDatabase
import com.kiper.core.data.source.remote.AudioRemoteDataSource
import com.kiper.core.data.source.remote.CapsuleRemoteDataSource
import com.kiper.core.data.source.remote.UpdateAppRemoteDataSource
import com.kiper.core.domain.repository.AudioRepository
import com.kiper.core.domain.repository.CapsuleRepository
import com.kiper.core.domain.repository.UpdateRepository
import com.kiper.core.domain.usecase.CheckAndDownloadVersionUseCase
import com.kiper.core.domain.usecase.DeleteAllRecordingsUseCase
import com.kiper.core.domain.usecase.GetCapsuleMessageUseCase
import com.kiper.core.domain.usecase.GetDeviceSchedulesUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.SaveRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import com.kiper.core.framework.audioRecorder.AndroidAudioRecorder
import com.kiper.core.framework.audioRecorder.AudioRecorder
import com.kiper.core.util.FileUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS)

        return OkHttpClient.Builder()
            .addInterceptor(ConditionalLoggingInterceptor())
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.kiperconnect.com/device/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideAudioRemoteDataSource(
        apiService: ApiService,
        scheduleDao: ScheduleDao,
        localDataSource: AudioLocalDataSource,
    ): AudioRemoteDataSource {
        return AudioRemoteDataSource(
            apiService = apiService,
            scheduleDao = scheduleDao,
            localDataSource = localDataSource
        )
    }

    @Provides
    @Singleton
    fun provideUpdateAppRemoteDataSource(
        apiService: ApiService,
        context: Context,
    ): UpdateAppRemoteDataSource {
        return UpdateAppRemoteDataSource(apiService = apiService, context = context)
    }

    @Provides
    @Singleton
    fun provideCapsuleRemoteDataSource(
        apiService: ApiService,
    ): CapsuleRemoteDataSource {
        return CapsuleRemoteDataSource(apiService = apiService)
    }

    @Provides
    @Singleton
    fun provideCapsuleRepository(
        capsuleRemoteDataSource: CapsuleRemoteDataSource,
    ): CapsuleRepository {
        return CapsuleRepositoryImpl(capsuleRemoteDataSource = capsuleRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUpdateRepository(
        updateAppRemoteDataSource: UpdateAppRemoteDataSource,
    ): UpdateRepository {
        return UpdateRepositoryImpl(updateAppRemoteDataSource = updateAppRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideAudioRepository(
        remoteDataSource: AudioRemoteDataSource,
        localDataSource: AudioLocalDataSource,
    ): AudioRepository {
        return AudioRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(@ApplicationContext context: Context): AudioRecorder {
        return AndroidAudioRecorder(
            context = context
        )
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideGetDeviceSchedulesUseCase(audioRepository: AudioRepository): GetDeviceSchedulesUseCase {
        return GetDeviceSchedulesUseCase(audioRepository = audioRepository)
    }


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAudioRecordingDao(appDatabase: AppDatabase): AudioRecordingDao {
        return appDatabase.audioRecordingDao()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(appDatabase: AppDatabase): ScheduleDao {
        return appDatabase.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideGetCapsuleMessageUseCase(capsuleRepository: CapsuleRepository): GetCapsuleMessageUseCase {
        return GetCapsuleMessageUseCase(capsuleRepository = capsuleRepository)
    }

    @Provides
    @Singleton
    fun provideUploadAudioUseCase(repository: AudioRepository): UploadAudioUseCase {
        return UploadAudioUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveRecordingUseCase(repository: AudioRepository): SaveRecordingUseCase {
        return SaveRecordingUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRecordingsForDayUseCase(repository: AudioRepository): GetRecordingsForDayUseCase {
        return GetRecordingsForDayUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteRecordingUseCase(repository: AudioRepository): DeleteAllRecordingsUseCase {
        return DeleteAllRecordingsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCheckAndDownloadUpdateUseCase(updateRepository: UpdateRepository): CheckAndDownloadVersionUseCase {
        return CheckAndDownloadVersionUseCase(updateRepository = updateRepository)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFileUtil(): FileUtil {
        return FileUtil()
    }

}