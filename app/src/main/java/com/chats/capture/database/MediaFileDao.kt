package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.MediaFile
import com.chats.capture.models.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {
    
    @Query("SELECT * FROM media_files WHERE uploadStatus = :status ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getMediaFilesByStatus(status: UploadStatus, limit: Int = 50): List<MediaFile>
    
    @Query("SELECT * FROM media_files WHERE notificationId = :notificationId")
    suspend fun getMediaFilesByNotification(notificationId: String): List<MediaFile>
    
    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getMediaFileById(id: String): MediaFile?
    
    @Query("SELECT * FROM media_files WHERE uploadStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingUploads(limit: Int = 50): List<MediaFile>
    
    @Query("SELECT COUNT(*) FROM media_files WHERE uploadStatus IN ('PENDING', 'FAILED')")
    suspend fun getPendingUploadCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFile(mediaFile: MediaFile)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFiles(mediaFiles: List<MediaFile>)
    
    @Update
    suspend fun updateMediaFile(mediaFile: MediaFile)
    
    @Query("UPDATE media_files SET uploadStatus = :status, remoteUrl = :url WHERE id = :id")
    suspend fun markAsUploaded(id: String, status: UploadStatus, url: String?)
    
    @Query("UPDATE media_files SET uploadStatus = :status, uploadAttempts = uploadAttempts + 1, lastUploadAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun markUploadAttempt(id: String, status: UploadStatus, timestamp: Long, error: String?)
    
    @Query("UPDATE media_files SET uploadStatus = 'PENDING' WHERE uploadStatus = 'UPLOADING' AND lastUploadAttempt < :beforeTimestamp")
    suspend fun resetStuckUploads(beforeTimestamp: Long)
    
    @Query("DELETE FROM media_files WHERE uploadStatus = 'SUCCESS' AND createdAt < :beforeTimestamp")
    suspend fun deleteOldUploadedFiles(beforeTimestamp: Long)
    
    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deleteMediaFile(id: String)
}
