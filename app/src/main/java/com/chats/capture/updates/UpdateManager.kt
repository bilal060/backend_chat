package com.chats.capture.updates

import android.content.Context
import com.chats.capture.database.UpdateStatusDao
import com.chats.capture.models.UpdateStatus
import com.chats.capture.models.UpdateStatusEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class UpdateManager(
    private val context: Context,
    private val updateStatusDao: UpdateStatusDao
) {
    
    private val updateChecker = UpdateChecker(context)
    private val updateDownloader = UpdateDownloader(context)
    private val updateInstaller = UpdateInstaller(context)
    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    suspend fun performUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            // Update status to CHECKING
            updateStatus(UpdateStatusEnum.CHECKING)
            
            // Check for updates
            val updateInfo = updateChecker.checkForUpdates()
            
            if (updateInfo == null) {
                updateStatus(UpdateStatusEnum.IDLE)
                return@withContext UpdateResult.NO_UPDATE_AVAILABLE
            }
            
            // Check if permission is granted
            if (!updateInstaller.canInstallPackages()) {
                updateStatus(UpdateStatusEnum.IDLE)
                return@withContext UpdateResult.PERMISSION_REQUIRED
            }
            
            // Download update
            updateStatus(UpdateStatusEnum.DOWNLOADING)
            val downloadResult = updateDownloader.downloadUpdate(updateInfo) { downloaded, total ->
                val progress = ((downloaded * 100) / total).toInt()
                updateScope.launch {
                    updateStatus(UpdateStatusEnum.DOWNLOADING, progress = progress)
                }
            }
            
            downloadResult.fold(
                onSuccess = { apkFile ->
                    // Install update
                    updateStatus(UpdateStatusEnum.INSTALLING)
                    val installResult = updateInstaller.installUpdate(apkFile)
                    
                    installResult.fold(
                        onSuccess = {
                            updateStatus(UpdateStatusEnum.IDLE)
                            UpdateResult.UPDATE_INSTALLED
                        },
                        onFailure = { error ->
                            Timber.e(error, "Installation failed")
                            updateStatus(UpdateStatusEnum.FAILED)
                            UpdateResult.UPDATE_FAILED
                        }
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Download failed")
                    updateStatus(UpdateStatusEnum.FAILED)
                    UpdateResult.UPDATE_FAILED
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error performing update")
            updateStatus(UpdateStatusEnum.FAILED)
            UpdateResult.UPDATE_FAILED
        }
    }
    
    private suspend fun updateStatus(
        status: UpdateStatusEnum,
        progress: Int = 0
    ) {
        val currentStatus = updateStatusDao.getUpdateStatus() ?: UpdateStatus()
        val updatedStatus = currentStatus.copy(
            updateStatus = status,
            updateDownloadProgress = progress,
            lastCheckTime = System.currentTimeMillis()
        )
        updateStatusDao.updateUpdateStatus(updatedStatus)
    }
    
    enum class UpdateResult {
        NO_UPDATE_AVAILABLE,
        UPDATE_DOWNLOADED,
        UPDATE_INSTALLED,
        UPDATE_FAILED,
        PERMISSION_REQUIRED
    }
}
