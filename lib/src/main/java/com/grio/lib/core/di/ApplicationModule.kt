package com.grio.lib.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.grio.lib.features.BugSnap
import com.grio.lib.BuildConfig
import com.grio.lib.features.report.JiraRepository
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = context

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
                .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://example.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(createClient())
                .build()
    }

    @Provides
    @Singleton
    @Named("jira")
    fun provideJiraRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BugSnap.jiraUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(createClient())
            .build()
    }

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideJiraRepository(dataSource: JiraRepository.Network): JiraRepository = dataSource
}