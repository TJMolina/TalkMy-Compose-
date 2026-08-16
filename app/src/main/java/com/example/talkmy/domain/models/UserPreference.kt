package com.example.talkmy.domain.models

sealed class UserPreference <T : Any>{
    abstract var value: T

    //default values are really, really needed
    data class TextSize(override var value: Float = 20.0f) : UserPreference<Float>()
    data class Volume(override var value: Int = 10) : UserPreference<Int>()
    data class Speech(override var value: Float = 1.0f) : UserPreference<Float>()
    data class Velocity(override var value: Float = 1.0f) : UserPreference<Float>()
    data class Voice(override var value: String = "") : UserPreference<String>()
    data class NextTask(override var value: Boolean = false) : UserPreference<Boolean>()
    data class ClickParagraph(override var value: Boolean = false) : UserPreference<Boolean>()

    //Don't forget to add the new type to this list. it's really important!
    companion object {
        val allDefaults: List<UserPreference<*>>
            get() = listOf(
                TextSize(),
                Volume(),
                Speech(),
                Velocity(),
                Voice(),
                NextTask(),
                ClickParagraph()
            )
    }
}