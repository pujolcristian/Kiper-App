package com.kiper.core.util

import android.util.Log
import com.kiper.core.domain.model.ScheduleResponse
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val String.parseTime: Calendar
    get() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.parse(this) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
val Calendar.timeString: String
    get() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(time)
    }

fun String.getScheduledFromNameFile(): ScheduleResponse {
    val divFileName = split("_")
    val starTime = divFileName.getOrNull(1).orEmpty()
    val endTime =
        divFileName.getOrNull(2)?.split(".")?.getOrNull(0).orEmpty()
    Log.i("getScheduledFromNameFile", "starTime: $starTime, endTime: $endTime")
    return ScheduleResponse(starTime, endTime)
}

fun String.generateFileName(): String {
    val index = System.currentTimeMillis()
    val name = "$this.$index.3gp"
    return name
}


fun String.getRemainingDurationFromNameFile(): Long? {
    val now = Calendar.getInstance()
    val divFileName = this.split("_")
    val endTimeString = divFileName.getOrNull(2)?.split(".")?.getOrNull(0).orEmpty()
    val endTime = endTimeString.parseTime

    // Ajustar la fecha de endTime a hoy
    endTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
    endTime.set(Calendar.MONTH, now.get(Calendar.MONTH))
    endTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

    val remainingDuration = endTime.timeInMillis - now.timeInMillis
    Log.e("getRemaining", "remainingDuration: $remainingDuration, endTime: ${endTime.timeInMillis}, now: ${now.timeInMillis}")
    return remainingDuration
}