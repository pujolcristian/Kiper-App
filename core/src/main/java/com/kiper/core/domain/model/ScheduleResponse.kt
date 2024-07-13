package com.kiper.core.domain.model

import com.kiper.core.util.parseTime
import java.util.Calendar

class ScheduleResponse(
    val startTime: String,
    val endTime: String,
)

fun ScheduleResponse.isIntoSchedule(): Boolean {
    val now = Calendar.getInstance()

    val startTime = this.startTime.parseTime
    val endTime = this.endTime.parseTime

    // Ajustamos las fechas al día de hoy
    val today = Calendar.getInstance()
    startTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
    startTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
    startTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

    endTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
    endTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
    endTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

    println("Evaluating schedule: ${this.startTime} to ${this.endTime}")
    println("Current time: ${now.time}")
    println("Start time: ${now <= endTime} && ${now >= startTime}")

    return now <= endTime && now >= startTime
}