package com.chats.capture.managers

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.chats.capture.database.ChatDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AppUsageMonitor(private val context: Context) {
    
    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastForegroundApp: String? = null
    private var lastForegroundTime: Long = 0
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startMonitoring() {
        monitorScope.launch {
            while (true) {
                trackAppUsage()
                delay(TimeUnit.SECONDS.toMillis(5)) // Check every 5 seconds
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun trackAppUsage() {
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(time - TimeUnit.MINUTES.toMillis(1), time)
            
            var currentApp: String? = null
            var lastEventTime: Long = 0
            
            while (events.hasNextEvent()) {
                val event = UsageEvents.Event()
                events.getNextEvent(event)
                
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        currentApp = event.packageName
                        lastEventTime = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (currentApp == event.packageName) {
                            val duration = event.timeStamp - lastEventTime
                            logAppUsage(currentApp, duration)
                            currentApp = null
                        }
                    }
                }
            }
            
            // Track current foreground app
            val foregroundApp = getCurrentForegroundApp(usageStatsManager)
            if (foregroundApp != lastForegroundApp) {
                if (lastForegroundApp != null) {
                    val duration = System.currentTimeMillis() - lastForegroundTime
                    logAppUsage(lastForegroundApp, duration)
                }
                lastForegroundApp = foregroundApp
                lastForegroundTime = System.currentTimeMillis()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error tracking app usage")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getCurrentForegroundApp(usageStatsManager: UsageStatsManager): String? {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            time - TimeUnit.SECONDS.toMillis(5),
            time
        )
        
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }
    
    private fun logAppUsage(packageName: String?, duration: Long) {
        if (packageName != null && duration > 0) {
            Timber.d("App usage: $packageName for ${duration}ms")
            // Can store in database if needed
        }
    }
    
    fun isUsageStatsPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - TimeUnit.DAYS.toMillis(1),
                time
            )
            stats.isNotEmpty()
        } else {
            false
        }
    }
    
    fun requestUsageStatsPermission(): android.content.Intent {
        val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }
    
    fun getAppUsageStats(days: Int = 7): List<AppUsageStat> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getUsageStatsInternal(days)
        } else {
            emptyList()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getUsageStatsInternal(days: Int): List<AppUsageStat> {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - TimeUnit.DAYS.toMillis(days.toLong()),
                time
            )
            
            val packageManager = context.packageManager
            stats.map { usageStats ->
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    usageStats.packageName
                }
                
                AppUsageStat(
                    packageName = usageStats.packageName,
                    appName = appName,
                    totalTimeInForeground = usageStats.totalTimeInForeground,
                    lastTimeUsed = usageStats.lastTimeUsed,
                    launchCount = 0 // launchCount not available in UsageStats API
                )
            }.sortedByDescending { it.totalTimeInForeground }
        } catch (e: Exception) {
            Timber.e(e, "Error getting usage stats")
            emptyList()
        }
    }
}

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val launchCount: Int
)
