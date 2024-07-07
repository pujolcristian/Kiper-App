package com.kiper.app.presentation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.room.Room
import androidx.work.Configuration
import com.kiper.core.data.source.local.db.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class KiperApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    private lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-database"
        ).build()
    }
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}