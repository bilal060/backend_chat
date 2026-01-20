package com.chats.capture.updates

import android.content.Context
import com.chats.capture.BuildConfig
import com.chats.capture.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class UpdateChecker(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
            val updateServerUrl = prefs.getString(
                "update_server_url",
                context.getString(R.string.update_server_url)
            ) ?: return@withContext null
            
            val manifestUrl = if (updateServerUrl.endsWith("/")) {
                "${updateServerUrl}manifest.json"
            } else {
                "$updateServerUrl/manifest.json"
            }
            
            val request = Request.Builder()
                .url(manifestUrl)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Timber.w("Failed to fetch manifest: ${response.code}")
                return@withContext null
            }
            
            val body = response.body?.string() ?: return@withContext null
            val manifest = Gson().fromJson(body, UpdateManifest::class.java)
            
            // Compare version codes
            if (manifest.version_code > BuildConfig.VERSION_CODE) {
                Timber.d("Update available: ${manifest.latest_version} (${manifest.version_code})")
                return@withContext UpdateInfo(
                    versionName = manifest.latest_version,
                    versionCode = manifest.version_code,
                    downloadUrl = manifest.update_url,
                    fileSize = manifest.file_size,
                    checksum = manifest.checksum,
                    forceUpdate = manifest.force_update
                )
            }
            
            Timber.d("No update available. Current: ${BuildConfig.VERSION_CODE}, Latest: ${manifest.version_code}")
            null
        } catch (e: SocketTimeoutException) {
            // Network timeout - expected when server is unreachable or slow
            Timber.d("Update check timeout: ${e.message}")
            null
        } catch (e: UnknownHostException) {
            // Server not found - expected when offline or wrong URL
            Timber.d("Update server not found: ${e.message}")
            null
        } catch (e: java.net.ConnectException) {
            // Connection refused - expected when server is down
            Timber.d("Update server connection refused: ${e.message}")
            null
        } catch (e: Exception) {
            // Other errors - log at warning level for unexpected issues
            Timber.w(e, "Error checking for updates")
            null
        }
    }
}

data class UpdateManifest(
    val latest_version: String,
    val version_code: Int,
    val min_supported_version: String,
    val update_url: String,
    val file_size: Long,
    val checksum: String,
    val release_date: String,
    val force_update: Boolean,
    val update_message: String?
)

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val fileSize: Long,
    val checksum: String,
    val forceUpdate: Boolean
)
