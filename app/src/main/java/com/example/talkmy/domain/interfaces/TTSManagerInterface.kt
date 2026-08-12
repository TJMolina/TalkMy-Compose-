package com.example.talkmy.domain.interfaces

interface TTSManagerInterface {
    fun speak(text: String)
    fun stop()
    fun release()
}