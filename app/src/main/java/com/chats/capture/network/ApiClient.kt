package com.chats.capture.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import timber.log.Timber

object ApiClient {
    private var retrofit: Retrofit? = null
    private var baseUrl: String = "https://backend-chat-yq33.onrender.com/" // Default to Render server
    
    fun initialize(context: Context, serverUrl: String) {
        var url = serverUrl.trim()
        
        // Validate URL - reject localhost and invalid URLs
        if (url.contains("127.0.0.1") || url.contains("localhost") || 
            url.startsWith("http://https://") || url.isEmpty()) {
            Timber.tag("API_CLIENT").w("Invalid server URL: $url, using default: $baseUrl")
            url = baseUrl
        }
        
        // Normalize URL
        baseUrl = if (url.endsWith("/")) url else "$url/"
        
        // Ensure URL starts with http:// or https://
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            Timber.tag("API_CLIENT").w("URL missing protocol, adding https://: $baseUrl")
            baseUrl = "https://$baseUrl"
        }
        
        Timber.tag("API_CLIENT").i("Initializing ApiClient with base URL: $baseUrl")
        
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d("API: $message")
        }.apply {
            level = if (com.chats.capture.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        
        // Create lenient Gson instance to handle malformed JSON responses
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        Timber.tag("API_CLIENT").i("ApiClient initialized successfully with base URL: $baseUrl")
    }
    
    fun getApiService(): ApiService {
        if (retrofit == null) {
            throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
        }
        return retrofit!!.create(ApiService::class.java)
    }
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }
}
