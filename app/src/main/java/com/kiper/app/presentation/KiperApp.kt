package com.kiper.app.presentation

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.room.Room
import androidx.work.Configuration
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.kiper.app.network.NetworkMonitor
import com.kiper.core.data.source.local.db.AppDatabase
import com.kiper.core.util.Constants.PERIODIC_CHECK_NETWORK_DELAY
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.android.schedulers.AndroidSchedulers
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