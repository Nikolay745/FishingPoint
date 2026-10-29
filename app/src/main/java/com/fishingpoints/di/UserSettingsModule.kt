package com.fishingpoints.di

import com.fishingpoints.data.SharedPrefsUserSettingsDataSource
import com.fishingpoints.domain.UserSettingsDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSettingsModule {

    @Binds
    @Singleton
    abstract fun bindUserSettingsDataSource(
        impl: SharedPrefsUserSettingsDataSource
    ): UserSettingsDataSource
}