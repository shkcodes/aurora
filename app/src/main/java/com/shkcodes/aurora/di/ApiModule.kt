package com.shkcodes.aurora.di

import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.api.AuthApi
import com.shkcodes.aurora.api.AuthInterceptor
import com.shkcodes.aurora.api.NetworkErrorHandler
import com.shkcodes.aurora.api.StringProviderImpl
import com.shkcodes.aurora.api.UserApi
import com.shkcodes.aurora.api.adapters.DateTimeAdapter
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.util.ApiConstants
import com.shkcodes.aurora.util.ApiConstants.API_TIMEOUT
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideMoshi(dateTimeAdapter: DateTimeAdapter): Moshi {
        return Moshi.Builder().add(dateTimeAdapter).build()
    }

    @Provides
    @Singleton
    fun provideGsonConverterFactory(moshi: Moshi): MoshiConverterFactory {
        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(API_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(API_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(API_TIMEOUT, TimeUnit.SECONDS)
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshiConverterFactory: MoshiConverterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(ApiConstants.API_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(moshiConverterFactory)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create()

    @Provides
    @Singleton
    fun provideStringProvider(stringProvider: StringProviderImpl): StringProvider = stringProvider

    @Provides
    @Singleton
    fun provideErrorHandler(errorHandler: NetworkErrorHandler): ErrorHandler = errorHandler
}
