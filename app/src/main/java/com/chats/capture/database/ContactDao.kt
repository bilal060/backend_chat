package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.Contact

@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contacts WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getUnsyncedContacts(limit: Int = 100): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE (synced = 0 OR lastSynced IS NULL OR lastSynced < :sinceTimestamp) AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getContactsSince(sinceTimestamp: Long, limit: Int = 100): List<Contact>
    
    @Query("SELECT * FROM contacts ORDER BY timestamp DESC")
    suspend fun getAllContacts(): List<Contact>
    
    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber OR email = :email LIMIT 1")
    suspend fun findContact(phoneNumber: String?, email: String?): Contact?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)
    
    @Update
    suspend fun updateContact(contact: Contact)
    
    @Query("UPDATE contacts SET synced = 1, lastSynced = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: String, syncTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE contacts SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :attemptTime, errorMessage = :error WHERE id = :id")
    suspend fun markSyncAttempt(id: String, attemptTime: Long, error: String?)
    
    @Query("SELECT COUNT(*) FROM contacts WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getTotalCount(): Int
    
    @Query("DELETE FROM contacts WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldContacts(beforeTimestamp: Long)
}
