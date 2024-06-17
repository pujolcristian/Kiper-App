package com.kiper.app.presentation

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kiper.app.service.SyncService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KiperApp : Application()