package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.NotificationData
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationData>>
    
    @Query("SELECT * FROM notifications WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsyncedNotifications(limit: Int = 50): List<NotificationData>
    
    @Query("SELECT * FROM notifications WHERE (synced = 0 OR lastSynced IS NULL OR lastSynced < :sinceTimestamp) AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getNotificationsSince(sinceTimestamp: Long, limit: Int = 50): List<NotificationData>
    
    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationData?
    
    @Query("SELECT * FROM notifications WHERE appPackage = :appPackage ORDER BY timestamp DESC")
    fun getNotificationsByApp(appPackage: String): Flow<List<NotificationData>>
    
    @Query("SELECT * FROM notifications WHERE appPackage = :appPackage AND title = :title AND text = :text AND ABS(timestamp - :timestamp) < 2000")
    suspend fun findDuplicateNotification(appPackage: String, title: String?, text: String?, timestamp: Long): NotificationData?
    
    @Query("SELECT COUNT(*) FROM notifications WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationData)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationData>)
    
    @Update
    suspend fun updateNotification(notification: NotificationData)

    @Query("UPDATE notifications SET iconUrl = :iconUrl WHERE id = :id")
    suspend fun updateIconUrl(id: String, iconUrl: String?)
    
    @Query("UPDATE notifications SET synced = 1, syncAttempts = 0, errorMessage = NULL, lastSynced = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: String, syncTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE notifications SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun markSyncAttempt(id: String, timestamp: Long, error: String?)
    
    @Query("UPDATE notifications SET serverMediaUrls = :serverUrls WHERE id = :id")
    suspend fun updateServerMediaUrls(id: String, serverUrls: List<String>?)
    
    @Query("DELETE FROM notifications WHERE synced = 1 AND timestamp < :beforeTimestamp")
    suspend fun deleteOldSyncedNotifications(beforeTimestamp: Long)
    
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}
