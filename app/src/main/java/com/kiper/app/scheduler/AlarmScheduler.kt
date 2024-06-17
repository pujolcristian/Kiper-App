package com.kiper.app.scheduler
/*
import android.content.Context
import com.kiper.app.jobs.AudioRecordJob
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val recordingSchedules = listOf(
        Pair(7, 0),  // 7:00 AM
        Pair(20, 14), // 12:00 PM
        Pair(24, 0)  // 4:00 PM
    )

    fun scheduleAlarms() {
        recordingSchedules

            .forEach { schedule ->
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.first)
                set(Calendar.MINUTE, schedule.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val delay = startTime.timeInMillis - System.currentTimeMillis()
            if (delay > 0) {
                AudioRecordJob.scheduleJob(context, delay)
            }
        }
    }
}

 */
