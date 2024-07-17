package com.kiper.app.di

import android.content.Context
import com.kiper.app.network.NetworkMonitor
import com.kiper.app.service.SyncViewModel
import com.kiper.core.domain.usecase.DeleteRecordingUseCase
import com.kiper.core.domain.usecase.GetDeviceSchedulesUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.SaveRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSyncViewModel(
        getDeviceSchedulesUseCase: GetDeviceSchedulesUseCase,
        getRecordingsForDayUseCase: GetRecordingsForDayUseCase,
        saveRecordingUseCase: SaveRecordingUseCase,
        uploadAudioUseCase: UploadAudioUseCase,
    ): SyncViewModel {
        return SyncViewModel(
            getDeviceSchedulesUseCase = getDeviceSchedulesUseCase,
            getRecordingsForDayUseCase = getRecordingsForDayUseCase,
            saveRecordingUseCase = saveRecordingUseCase,
            uploadAudioUseCase = uploadAudioUseCase
        )
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }
}