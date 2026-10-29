package com.fishingpoints.data.local.database

import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.FishingCatch
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import com.fishingpoints.data.local.entity.EquipmentEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.database.dao.EquipmentDao
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.FishingBanDao
import com.fishingpoints.data.local.database.dao.FishingCatchDao
import com.fishingpoints.data.local.database.dao.FishingEntryDao
import com.fishingpoints.data.local.database.dao.HookDao
import com.fishingpoints.data.local.database.dao.LocationDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.database.dao.RegionRuleDao
import com.fishingpoints.data.local.database.dao.StatisticsDao
import com.fishingpoints.data.local.database.dao.TackleDao
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishSeasonCrossRef
import com.fishingpoints.data.local.entity.FishTimeOfDayCrossRef
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.FishingEntriesBaitCrossRef
import com.fishingpoints.data.local.entity.Hook
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.entity.RegionRule

@Database(
    entities = [
        Region::class,
        RegionRule::class,
        LocationEntity::class,
        Fish::class,
        Bait::class,
        Tackle::class,
        Hook::class,
        BaitFishCrossRef::class,
        TackleFishCrossRef::class,
        FishingEntry::class,
        FishingCatch::class,
        FishingBan::class,
        DailyAllowance::class,
        FishSeasonCrossRef::class,
        FishTimeOfDayCrossRef::class,
        FishingEntriesBaitCrossRef::class,
        Feed::class,
        FeedFishCrossRef::class,
        EquipmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FishingDatabase : RoomDatabase() {

    abstract fun fishDao(): FishDao
    abstract fun baitDao(): BaitDao
    abstract fun feedDao(): FeedDao
    abstract fun tackleDao(): TackleDao
    abstract fun hookDao(): HookDao
    abstract fun fishingEntryDao(): FishingEntryDao
    abstract fun fishingCatchDao(): FishingCatchDao
    abstract fun fishingBanDao(): FishingBanDao
    abstract fun locationDao(): LocationDao
    abstract fun regionDao(): RegionDao
    abstract fun regionRuleDao(): RegionRuleDao
    abstract fun dailyAllowanceDao(): DailyAllowanceDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun equipmentDao(): EquipmentDao
}
