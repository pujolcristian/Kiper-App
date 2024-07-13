package com.kiper.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.kiper.core.data.ApiService
import com.kiper.core.data.repository.AudioRepositoryImpl
import com.kiper.core.data.repository.NetworkRepositoryImpl
import com.kiper.core.data.source.local.dao.AudioRecordingDao
import com.kiper.core.data.source.local.dao.ScheduleDao
import com.kiper.core.data.source.local.db.AppDatabase
import com.kiper.core.data.source.remote.AudioLocalDataSource
import com.kiper.core.data.source.remote.AudioRemoteDataSource
import com.kiper.core.domain.repository.AudioRepository
import com.kiper.core.domain.repository.NetworkRepository
import com.kiper.core.domain.usecase.DeleteRecordingUseCase
import com.kiper.core.domain.usecase.GetDeviceSchedulesUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.SaveRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import com.kiper.core.framework.worker.audioRecorder.AndroidAudioRecorder
import com.kiper.core.framework.worker.audioRecorder.AudioRecorder
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
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
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
    fun provideNetworkRepository(@ApplicationContext context: Context): NetworkRepository {
        return NetworkRepositoryImpl(context)
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
    fun provideDeleteRecordingUseCase(repository: AudioRepository): DeleteRecordingUseCase {
        return DeleteRecordingUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

}