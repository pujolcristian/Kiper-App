package com.kiper.app.service

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kiper.app.presentation.MainActivity
@SuppressLint("SpecifyJobSchedulerIdRange")
class RestartJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val restartIntent = Intent(applicationContext, MainActivity::class.java)
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(applicationContext, restartIntent, null)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }
}