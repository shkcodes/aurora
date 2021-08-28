package com.shkcodes.aurora.di

import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.api.NetworkErrorHandler
import com.shkcodes.aurora.api.StringProviderImpl
import com.shkcodes.aurora.api.TwitterApi
import com.shkcodes.aurora.api.adapters.LocalDateTimeAdapter
import com.shkcodes.aurora.api.adapters.ZonedDateTimeAdapter
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.EventBusImpl
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.service.DefaultTimeProvider
import com.shkcodes.aurora.service.TimeProvider
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import twitter4j.TwitterFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideTwitterApi(configuration: Configuration): TwitterApi {
        return TwitterFactory(configuration).instance
    }

    @Provides
    @Singleton
    fun provideMoshi(
        zonedDateTimeAdapter: ZonedDateTimeAdapter,
        localDateTimeAdapter: LocalDateTimeAdapter
    ): Moshi {
        return Moshi.Builder()
            .add(zonedDateTimeAdapter)
            .add(localDateTimeAdapter)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiConfiguration(): Configuration {
        return ConfigurationBuilder().apply {
            setOAuthConsumerKey(BuildConfig.CONSUMER_KEY)
            setOAuthConsumerSecret(BuildConfig.CONSUMER_SECRET)
            setTweetModeExtended(true)
        }.build()
    }

    @Provides
    @Singleton
    fun provideStringProvider(stringProvider: StringProviderImpl): StringProvider = stringProvider

    @Provides
    @Singleton
    fun provideErrorHandler(errorHandler: NetworkErrorHandler): ErrorHandler = errorHandler

    @Provides
    @Singleton
    fun provideTimeProvider(timeProvider: DefaultTimeProvider): TimeProvider = timeProvider

    @Provides
    @Singleton
    fun provideEventBus(eventBus: EventBusImpl): EventBus = eventBus
}
