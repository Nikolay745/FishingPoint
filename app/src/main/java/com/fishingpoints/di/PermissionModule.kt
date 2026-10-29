package com.fishingpoints.di


import com.fishingpoints.domain.permissions.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Provides
    @Singleton
    fun providePermissionManager(
        context: android.content.Context
    ): PermissionManager {
        return PermissionManager(context)
    }
}