package com.shkcodes.aurora.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.shkcodes.aurora.cache.AuroraDatabase
import com.shkcodes.aurora.util.CacheConstants.PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideDatabase(application: Application): AuroraDatabase {
        return Room.databaseBuilder(
            application,
            AuroraDatabase::class.java,
            AuroraDatabase.NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTweetsDao(database: AuroraDatabase) = database.tweetsDao()
}
