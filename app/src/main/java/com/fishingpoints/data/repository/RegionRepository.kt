package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.presentation.screens.regions.RegionSaveResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionRepository @Inject constructor(
    private val dao: RegionDao
) {
    fun getAllRegions(): Flow<List<Region>> = dao.getAllRegions()

    suspend fun getRegionById(id: Long): Region? = dao.getRegionById(id)

    suspend fun delete(region: Region) = dao.delete(region)
    suspend fun getRegionsByNameContains(query: String): List<Region> =
        dao.getRegionsByNameContains(query)

    fun getCount(): Flow<Int> = dao.getCount()

    suspend fun insert(region: Region): RegionSaveResult {
        val normalizedName = normalizeName(region.name)
        if (normalizedName.isBlank()) {
            return RegionSaveResult.EmptyName
        }

        val existing = dao.getRegionByNameIgnoreCase(normalizedName)
        if (existing != null) {
            return RegionSaveResult.DuplicateName
        }

        try {
            dao.insert(
                region.copy(
                    name = normalizedName
                )
            )
            return RegionSaveResult.Success

        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Защита от гонки двух одновременных insert
            return RegionSaveResult.DuplicateName
        }
    }

    suspend fun update(region: Region): RegionSaveResult {
        val normalizedName = normalizeName(region.name)

        if (normalizedName.isBlank()) {
            return RegionSaveResult.EmptyName
        }

        val existing = dao.getRegionByNameIgnoreCaseExceptId(
            name = normalizedName,
            id = region.id
        )

        if (existing != null) {
            return RegionSaveResult.DuplicateName
        }

        try {
            dao.update(
                region.copy(
                    name = normalizedName
                )
            )

            return RegionSaveResult.Success

        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            return RegionSaveResult.DuplicateName
        }
    }


    suspend fun getOrCreateRegion(name: String): Long {
        val normalizedName = normalizeName(name)
        require(normalizedName.isNotBlank()) {
            "Название региона не может быть пустым"
        }

        val existing = dao.getRegionByNameIgnoreCase(normalizedName)

        if (existing != null) {
            return existing.id
        }

        return try {
            dao.insert(
                Region(
                    name = normalizedName
                )
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {

            // На случай гонки двух одновременных insert.
            dao.getRegionByNameIgnoreCase(
                normalizedName
            )?.id
                ?: throw e
        }
    }

    private fun normalizeName(
        name: String
    ): String =
        name
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
}


