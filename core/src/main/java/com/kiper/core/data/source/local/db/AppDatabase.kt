package com.kiper.core.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kiper.core.data.source.local.dao.AudioRecordingDao
import com.kiper.core.data.source.local.dao.ScheduleDao
import com.kiper.core.data.source.local.entity.AudioRecordingEntity
import com.kiper.core.data.source.local.entity.ScheduleEntity
import com.kiper.core.util.Converters

@Database(entities = [AudioRecordingEntity::class, ScheduleEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioRecordingDao(): AudioRecordingDao
    abstract fun scheduleDao(): ScheduleDao
}
