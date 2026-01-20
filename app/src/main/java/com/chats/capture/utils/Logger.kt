package com.chats.capture.utils

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        
        when (priority) {
            Log.ERROR -> Log.e(tag, message, t)
            Log.WARN -> Log.w(tag, message, t)
            Log.INFO -> Log.i(tag, message, t)
            else -> Log.d(tag, message, t)
        }
    }
}
