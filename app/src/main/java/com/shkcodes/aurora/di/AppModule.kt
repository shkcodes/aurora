package com.shkcodes.aurora.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.room.Room
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.cache.AuroraDatabase
import com.shkcodes.aurora.util.CacheConstants.PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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

    @Provides
    @Singleton
    fun provideUsersDao(database: AuroraDatabase) = database.usersDao()

    @Provides
    @Singleton
    fun provideImageLoader(application: Application) = ImageLoader.Builder(application)
        .componentRegistry {
            if (SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder(application))
            } else {
                add(GifDecoder())
            }
        }
        .build()

    @Singleton
    @Provides
    fun providesCoroutineScope(dispatcherProvider: DispatcherProvider): CoroutineScope {
        return CoroutineScope(SupervisorJob() + dispatcherProvider.default)
    }
}
