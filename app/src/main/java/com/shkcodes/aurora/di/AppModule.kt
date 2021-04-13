package com.shkcodes.aurora.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
}
