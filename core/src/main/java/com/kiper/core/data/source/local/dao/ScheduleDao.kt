package com.kiper.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiper.core.data.source.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(schedules: List<ScheduleEntity>)

    @Query("SELECT * FROM schedules")
    fun getAll(): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}

