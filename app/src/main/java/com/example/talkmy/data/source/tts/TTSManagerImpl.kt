package com.example.talkmy.data.source.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.talkmy.domain.interfaces.TTSManagerInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TTSManagerInterface, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            Log.d("TTSManager", "TTS Initialized successfully")
            // Speak any pending text that was requested during initialization
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        } else {
            Log.e("TTSManager", "TTS Initialization failed")
        }
    }

    override fun speak(text: String) {
        if (tts == null) {
            Log.d("TTSManager", "First speak call - Initializing TTS engine...")
            pendingText = text
            tts = TextToSpeech(context, this)
            return
        }

        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.d("TTSManager", "TTS not ready yet, queuing text")
            pendingText = text
        }
    }

    override fun stop() {
        tts?.stop()
        pendingText = null
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        pendingText = null
    }
}