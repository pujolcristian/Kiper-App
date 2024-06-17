package com.kiper.core.di

import android.content.Context
import androidx.work.WorkManager
import com.kiper.core.audio.AudioRecorder
import com.kiper.core.data.repository.AudioRepositoryImpl
import com.kiper.core.data.repository.NetworkRepositoryImpl
import com.kiper.core.domain.repository.AudioRepository
import com.kiper.core.domain.repository.NetworkRepository
import com.kiper.core.domain.usecase.StartRecordingUseCase
import com.kiper.core.domain.usecase.StopRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideAudioRecorder(@ApplicationContext context: Context): AudioRecorder {
        return AudioRecorder()
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
    fun provideAudioRepository(@ApplicationContext context: Context): AudioRepository {
        return AudioRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideStartRecordingUseCase(audioRepository: AudioRepository): StartRecordingUseCase {
        return StartRecordingUseCase(audioRepository = audioRepository)
    }

    @Provides
    @Singleton
    fun provideStopRecordingUseCase(audioRepository: AudioRepository): StopRecordingUseCase {
        return StopRecordingUseCase(audioRepository = audioRepository)
    }

    @Provides
    @Singleton
    fun provideUploadAudioUseCase(networkRepository: NetworkRepository): UploadAudioUseCase {
        return UploadAudioUseCase(networkRepository = networkRepository)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

}