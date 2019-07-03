package com.grio.lib.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.grio.lib.features.BugSnap
import com.grio.lib.BuildConfig
import com.grio.lib.features.editor.JiraRepository
import dagger.Module
import dagger.Provides
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
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
        okHttpClientBuilder.addInterceptor( BasicAuthInterceptor(BugSnap.jiraUsername, BugSnap.jiraApiKey))
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideJiraRepository(dataSource: JiraRepository.Network): JiraRepository = dataSource
}

class BasicAuthInterceptor(user: String, password: String) : Interceptor {

    private val credentials: String = Credentials.basic(user, password)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }

}