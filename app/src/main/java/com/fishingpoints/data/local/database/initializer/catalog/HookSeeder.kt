package com.fishingpoints.data.local.database.initializer.catalog


import com.fishingpoints.data.local.database.dao.HookDao
import com.fishingpoints.data.local.entity.Hook
import javax.inject.Inject

internal class HookSeeder @Inject constructor(
    private val hookDao: HookDao
) {
    suspend fun seedIfEmpty() {
        if (hookDao.getHookCount() != 0) return
        HookData.all.forEach { hookDao.insertHook(it) }
    }
}

internal object HookData {
    val all: List<Hook> = listOf(
        Hook(number = "3/0", numberRu = "16", sase = "16", saseRu = "16", description = " "),
        Hook(number = "2/0", numberRu = "14", sase = "14", saseRu = "14", description = " "),
        Hook(number = "1/0", numberRu = "12", sase = "12", saseRu = "12", description = " "),
        Hook(number = "1", numberRu = "10", sase = "10", saseRu = "10", description = " "),
        Hook(number = "2", numberRu = null, sase = "9", saseRu = null, description = ""),
        Hook(number = "3", numberRu = null, sase = "8.5", saseRu = null, description = ""),
        Hook(number = "4", numberRu = "8", sase = "8", saseRu = "8", description = ""),
        Hook(number = "5", numberRu = null, sase = "7.5", saseRu = null, description = ""),
        Hook(number = "6", numberRu = "7", sase = "7", saseRu = "7", description = ""),
        Hook(number = "7", numberRu = null, sase = "6.5", saseRu = null, description = ""),
        Hook(number = "8", numberRu = "6", sase = "6.0", saseRu = "6.0", description = ""),
        Hook(number = "9", numberRu = null, sase = "5.5", saseRu = null, description = ""),
        Hook(number = "10", numberRu = "5", sase = "5.0", saseRu = "5.0", description = ""),
        Hook(number = "11", numberRu = "4.5", sase = "4.6", saseRu = "4.5", description = ""),
        Hook(number = "12", numberRu = null, sase = "4.5", saseRu = null, description = ""),
        Hook(number = "13", numberRu = "4", sase = "4.0", saseRu = "4.0", description = ""),
        Hook(number = "14", numberRu = null, sase = "3.6", saseRu = null, description = ""),
        Hook(number = "15", numberRu = "3.5", sase = "3.4", saseRu = "3.5", description = ""),
        Hook(number = "16", numberRu = null, sase = "3.2", saseRu = null, description = ""),
        Hook(number = "17", numberRu = "3", sase = "3", saseRu = "3", description = ""),
        Hook(number = "18", numberRu = "2.5", sase = "2.6", saseRu = "2.5", description = ""),
        Hook(number = "19", numberRu = null, sase = "2.4", saseRu = null, description = ""),
        Hook(number = "20", numberRu = null, sase = "2.2", saseRu = null, description = ""),
        Hook(number = "22", numberRu = null, sase = "2", saseRu = null, description = "")
    )
}