package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fishingpoints.data.local.entity.Hook
import kotlinx.coroutines.flow.Flow

@Dao
interface HookDao {
    @Query("SELECT * FROM hooks ORDER BY id")
    fun getAllHooks(): Flow<List<Hook>>

    @Query("SELECT COUNT(*) FROM hooks ORDER BY id")
    suspend fun getHookCount(): Int

    @Insert
    suspend fun insertHook(hook: Hook): Long

}