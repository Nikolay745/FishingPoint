package com.fishingpoints.di

import com.fishingpoints.data.local.database.FishingDatabase
import com.fishingpoints.data.repository.StatisticsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StatisticsModule {

    @Provides
    @Singleton
    fun provideStatisticsRepository(
        database: FishingDatabase
    ): StatisticsRepository {
        return StatisticsRepository(
            database.statisticsDao()
        )
    }
}