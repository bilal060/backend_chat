package com.chats.controller.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import timber.log.Timber

object ApiClient {
    private var retrofit: Retrofit? = null
    private var baseUrl: String = "https://backend-chat-yq33.onrender.com/"
    
    fun initialize(context: Context, serverUrl: String) {
        baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d("API: $message")
        }.apply {
            level = if (com.chats.controller.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        Timber.d("ApiClient initialized with base URL: $baseUrl")
    }
    
    fun getApiService(): ControllerApiService {
        if (retrofit == null) {
            throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
        }
        return retrofit!!.create(ControllerApiService::class.java)
    }
    
    fun updateBaseUrl(context: Context, serverUrl: String) {
        initialize(context, serverUrl)
    }
}
