package com.chats.controller.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AuthInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Get JWT token from SharedPreferences
        val prefs = context.getSharedPreferences("controller_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        val response = chain.proceed(newRequest)
        
        // Handle 401 Unauthorized - token expired or invalid
        if (response.code == 401) {
            Timber.w("Received 401 Unauthorized - clearing auth token")
            prefs.edit().remove("auth_token").apply()
        }
        
        return response
    }
}
