package com.kiper.core.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kiper.core.data.source.local.dao.AudioFileDao
import com.kiper.core.data.source.local.dao.ScheduleDao
import com.kiper.core.data.source.local.entity.AudioFile
import com.kiper.core.data.source.local.entity.Schedule

@Database(entities = [AudioFile::class, Schedule::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
    abstract fun scheduleDao(): ScheduleDao
}