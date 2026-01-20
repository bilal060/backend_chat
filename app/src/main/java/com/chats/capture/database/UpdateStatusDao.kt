package com.chats.capture.database

import androidx.room.*
import com.chats.capture.models.UpdateStatus

@Dao
interface UpdateStatusDao {
    
    @Query("SELECT * FROM update_status WHERE id = 1")
    suspend fun getUpdateStatus(): UpdateStatus?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateStatus(status: UpdateStatus)
    
    @Update
    suspend fun updateUpdateStatus(status: UpdateStatus)
}
