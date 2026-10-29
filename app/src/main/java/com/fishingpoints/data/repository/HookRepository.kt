package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.HookDao
import com.fishingpoints.data.local.entity.Hook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HookRepository @Inject constructor(
    private val hookDao: HookDao
) {

    val tackles: Flow<List<Hook>> = hookDao.getAllHooks()

    suspend fun insertHook(hook: Hook): Long {
        return hookDao .insertHook(hook)
    }
}