package com.chats.capture.utils

import android.content.Context
import timber.log.Timber

/**
 * Caches chat/notification icon capture so it is done only once per chat key.
 */
object IconCacheManager {

    private const val PREFS_NAME = "icon_cache"
    private const val KEY_SET = "captured_icon_keys"

    fun hasIcon(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SET, emptySet()) ?: emptySet()
        return set.contains(key)
    }

    fun markIconCaptured(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_SET, emptySet()) ?: emptySet()
        val updated = existing.toMutableSet()
        updated.add(key)
        prefs.edit().putStringSet(KEY_SET, updated).apply()
        Timber.d("Icon cached for key: $key")
    }
}
