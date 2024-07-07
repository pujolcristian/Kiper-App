package com.kiper.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Util {

    fun parseTime(time: String): Calendar {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.parse(time) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
}