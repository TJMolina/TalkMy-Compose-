package com.example.talkmy.di

import com.example.talkmy.data.network.CloudflareKiller
import com.example.talkmy.data.network.USER_AGENT
import com.example.talkmy.data.network.ignoreAllSSLErrors
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.ResponseParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.reflect.KClass

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @BaseClient
    @Provides
    @Singleton
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .ignoreAllSSLErrors()
            .readTimeout(30L, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @BaseClient baseClient: OkHttpClient,
        cloudflareKiller: CloudflareKiller
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(cloudflareKiller)
            .build()
    }

    @Provides
    @Singleton
    fun provideResponseParser(): ResponseParser {
        return object : ResponseParser {
            val mapper: ObjectMapper = jacksonObjectMapper().configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
            )

            override fun <T : Any> parse(text: String, kClass: KClass<T>): T {
                return mapper.readValue(text, kClass.java)
            }

            override fun <T : Any> parseSafe(text: String, kClass: KClass<T>): T? {
                return try {
                    mapper.readValue(text, kClass.java)
                } catch (e: Exception) {
                    null
                }
            }

            override fun writeValueAsString(obj: Any): String {
                return mapper.writeValueAsString(obj)
            }
        }
    }

    @Provides
    @Singleton
    fun provideRequests(
        okHttpClient: OkHttpClient,
        @BaseClient baseClient: OkHttpClient, // We might need a way to pass the base client to CF killer
        responseParser: ResponseParser
    ): Requests {
        return Requests(
            baseClient = okHttpClient,
            responseParser = responseParser
        ).apply {
            defaultHeaders = mapOf("user-agent" to USER_AGENT)
        }
    }
}