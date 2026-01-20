package com.chats.capture.updates

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.chats.capture.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class UpdateInstaller(private val context: Context) {
    
    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // Pre-Oreo, assume can install
        }
    }
    
    suspend fun installUpdate(apkFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                installWithPackageInstaller(apkFile)
            } else {
                installWithIntent(apkFile)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error installing update")
            Result.failure(e)
        }
    }
    
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private suspend fun installWithPackageInstaller(apkFile: File): Result<Boolean> {
        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val sessionId = packageInstaller.createSession(
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            )
            
            val session = packageInstaller.openSession(sessionId)
            FileInputStream(apkFile).use { input ->
                session.openWrite("app.apk", 0, -1).use { output ->
                    input.copyTo(output)
                }
            }
            
            val intent = Intent(context, UpdateInstallReceiver::class.java).apply {
                putExtra("session_id", sessionId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            session.commit(pendingIntent.intentSender)
            session.close()
            
            Timber.d("Installation started via PackageInstaller")
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error with PackageInstaller")
            Result.failure(e)
        }
    }
    
    private suspend fun installWithIntent(apkFile: File): Result<Boolean> {
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
            Timber.d("Installation started via Intent")
            Result.success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error with Intent installation")
            Result.failure(e)
        }
    }
}

// Broadcast receiver for installation result
class UpdateInstallReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Timber.d("Update installed successfully")
                // Restart app
                val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                restartIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(restartIntent)
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Timber.e("Installation failed: $message")
            }
        }
    }
}
