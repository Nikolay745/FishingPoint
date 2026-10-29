package com.fishingpoints.di

import com.fishingpoints.data.local.database.FishingDatabase
import android.content.Context
import androidx.room.Room
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
import com.fishingpoints.data.local.database.dao.TackleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class) object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FishingDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FishingDatabase::class.java,
            "fishing_database"
        ).build()
    }

    @Provides
    fun provideFishDao(db: FishingDatabase): FishDao = db.fishDao()

    @Provides
    fun provideBaitDao(db: FishingDatabase): BaitDao = db.baitDao()

    @Provides
    fun provideFeedDao(db: FishingDatabase): FeedDao = db.feedDao()

    @Provides
    fun provideTackleDao(db: FishingDatabase): TackleDao = db.tackleDao()

    @Provides
    fun provideHookDao(db: FishingDatabase): HookDao = db.hookDao()

    @Provides
    fun provideFishingEntryDao(db: FishingDatabase): FishingEntryDao =  db.fishingEntryDao()

    @Provides
    fun provideFishingCatchDao(db: FishingDatabase): FishingCatchDao =  db.fishingCatchDao()

    @Provides
    fun provideLocationDao(db: FishingDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideRegionDao(db: FishingDatabase): RegionDao = db.regionDao()

    @Provides
    fun provideRegionRuleDao(db: FishingDatabase): RegionRuleDao = db.regionRuleDao()

    @Provides
    fun provideDailyAllowanceDao(db: FishingDatabase): DailyAllowanceDao = db.dailyAllowanceDao()

    @Provides
    fun provideFishingBanDao(db: FishingDatabase): FishingBanDao = db.fishingBanDao()

    @Provides
    fun provideEquipmentDao(db: FishingDatabase): EquipmentDao = db.equipmentDao()
}