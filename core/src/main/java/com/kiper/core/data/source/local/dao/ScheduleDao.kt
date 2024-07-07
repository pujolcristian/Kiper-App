package com.kiper.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiper.core.data.source.local.entity.Schedule

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule)

    @Query("SELECT * FROM Schedule")
    suspend fun getSchedules(): List<Schedule>
}