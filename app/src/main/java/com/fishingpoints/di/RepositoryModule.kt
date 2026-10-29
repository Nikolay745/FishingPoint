package com.fishingpoints.di

import com.fishingpoints.data.local.database.FishingDatabase
import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.FishingCatchDao
import com.fishingpoints.data.local.database.dao.FishingEntryDao
import com.fishingpoints.data.local.database.dao.LocationDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.database.dao.TackleDao
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.FishingRepository
import com.fishingpoints.data.repository.JournalRepository
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.data.repository.RegionRepository
import com.fishingpoints.data.repository.TackleRepository
import com.fishingpoints.domain.lunar.LunarCalendar
import com.fishingpoints.domain.service.TipsGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLunarCalendar(): LunarCalendar = LunarCalendar()

    @Provides
    @Singleton
    fun provideTipsGenerator(): TipsGenerator = TipsGenerator()

    @Provides
    @Singleton
    fun provideRegionRepository(
        regionDao: RegionDao
    ): RegionRepository {
        return RegionRepository(dao = regionDao)
    }

    @Provides
    @Singleton
    fun provideFishingRepository(
        fishingEntryDao: FishingEntryDao,
        dailyAllowanceDao: DailyAllowanceDao,
        regionDao: RegionDao,
        fishingCatchDao: FishingCatchDao
    ): FishingRepository {
        return FishingRepository(
            fishingEntryDao = fishingEntryDao,
            dailyAllowanceDao = dailyAllowanceDao,
            regionDao = regionDao,
            fishingCatchDao = fishingCatchDao
        )
    }


    @Provides
        @Singleton
        fun provideLocationRepository(
            locationDao: LocationDao,
            regionRepository: RegionRepository
        ): LocationRepository {
            return LocationRepository(
                locationDao = locationDao,
                regionRepository = regionRepository
            )
        }

    @Provides
    @Singleton
    fun provideFishingCatchRepository(
        fishingCatchDao: FishingCatchDao,
    ): JournalRepository{
        return JournalRepository(
            fishingCatchDao,
        )
    }

    @Provides
    @Singleton
    fun provideTackleRepository(
        tackleDao: TackleDao,
    ): TackleRepository{
        return TackleRepository(
            tackleDao,
        )
    }

    @Provides
    @Singleton
    fun provideFishRepository(
        database: FishingDatabase,
        fishDao: FishDao,
        baitDao: BaitDao,
        tackleDao: TackleDao,
        feedDao: FeedDao
    ): FishRepository {
        return FishRepository(
            database = database,
            fishDao = fishDao,
            baitDao = baitDao,
            tackleDao = tackleDao,
            feedDao = feedDao
        )
    }

}