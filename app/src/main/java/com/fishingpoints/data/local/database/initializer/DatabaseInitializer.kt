package com.fishingpoints.data.local.database.initializer


import androidx.room.withTransaction
import com.fishingpoints.data.local.database.FishingDatabase
import com.fishingpoints.data.local.database.initializer.catalog.BaitSeeder
import com.fishingpoints.data.local.database.initializer.catalog.FeedSeeder
import com.fishingpoints.data.local.database.initializer.catalog.FishCrossRefSeeder
import com.fishingpoints.data.local.database.initializer.catalog.FishSeeder
import com.fishingpoints.data.local.database.initializer.catalog.HookSeeder
import com.fishingpoints.data.local.database.initializer.catalog.TackleSeeder
import com.fishingpoints.data.local.database.initializer.region.sverdlovsk.SverdlovskRegionSeeder
import com.fishingpoints.data.local.database.initializer.region.voronezh.VoronezhRegionSeeder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabaseInitializer @Inject  constructor(
    private val database: FishingDatabase,
    private val fishSeeder: FishSeeder,
    private val baitSeeder: BaitSeeder,
    private val tackleSeeder: TackleSeeder,
    private val feedSeeder: FeedSeeder,
    private val hookSeeder: HookSeeder,
    private val fishCrossRefSeeder: FishCrossRefSeeder,
    private val voronezhSeeder: VoronezhRegionSeeder,
    private val sverdlovskSeeder: SverdlovskRegionSeeder
) {
    suspend fun initializeDatabase() {
        database.withTransaction {
            fishSeeder.seedIfEmpty()
            baitSeeder.seedIfEmpty()
            tackleSeeder.seedIfEmpty()
            feedSeeder.seedIfEmpty()
            hookSeeder.seedIfEmpty()
            fishCrossRefSeeder.seedIfEmpty()   // зависит от рыб
            voronezhSeeder.seedIfEmpty()
            sverdlovskSeeder.seedIfEmpty()
        }
    }
}