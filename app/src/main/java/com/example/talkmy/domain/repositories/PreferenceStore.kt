package com.example.talkmy.domain.repositories

import com.example.talkmy.domain.models.AllPreferences
import com.example.talkmy.domain.models.PreferenceData
import com.example.talkmy.domain.models.PreferencesType

interface PreferenceStore {

    fun getString(key: String, defaultValue: String = ""): PreferenceData<String>

    fun getLong(key: String, defaultValue: Long = 0): PreferenceData<Long>

    fun getInt(key: String, defaultValue: Int = 0): PreferenceData<Int>

    fun getFloat(key: String, defaultValue: Float = 0f): PreferenceData<Float>

    fun getBoolean(key: String, defaultValue: Boolean = false): PreferenceData<Boolean>

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): PreferenceData<Set<String>>

    fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): PreferenceData<T>

    fun getAll(): Map<String, *>
}

inline fun <reified T : Enum<T>> PreferenceStore.getEnum(
    key: String,
    defaultValue: T,
): PreferenceData<T> {
    return getObject(
        key = key,
        defaultValue = defaultValue,
        serializer = { it.name },
        deserializer = {
            try {
                enumValueOf(it)
            } catch (e: IllegalArgumentException) {
                defaultValue
            }
        },
    )
}

// --- Extensions for shorter syntax ---

/**
 * Saves a value generically
 */
fun PreferenceStore.set(key: String, value: Any) {
    when (value) {
        is String -> getString(key).set(value)
        is Int -> getInt(key).set(value)
        is Boolean -> getBoolean(key).set(value)
        is Float -> getFloat(key).set(value)
        is Long -> getLong(key).set(value)
    }
}

fun PreferenceStore.set(type: PreferencesType, value: Any) = set(type.name, value)

/**
 * Gets a value generically
 */
@Suppress("UNCHECKED_CAST")
fun <T> PreferenceStore.get(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is String -> getString(key, defaultValue).get() as T
        is Int -> getInt(key, defaultValue).get() as T
        is Boolean -> getBoolean(key, defaultValue).get() as T
        is Float -> getFloat(key, defaultValue).get() as T
        is Long -> getLong(key, defaultValue).get() as T
        else -> defaultValue
    }
}

fun <T> PreferenceStore.get(type: PreferencesType, defaultValue: T): T = get(type.name, defaultValue)

/**
 * Maps all preferences to the AllPreferences model
 */
fun PreferenceStore.getAllTalkMyPreferences(): AllPreferences {
    return AllPreferences(
        textSize = get(PreferencesType.TEXTSIZE, 20f),
        volume = get(PreferencesType.VOLUME, 10),
        speech = get(PreferencesType.SPEECH, 1f),
        velocity = get(PreferencesType.VELOCITY, 1f),
        voice = get(PreferencesType.VOICE, ""),
        readNextTask = get(PreferencesType.NEXTTASK, false),
        saveOnline = get(PreferencesType.SAVEONLINE, false),
        clickParagraph = get(PreferencesType.CLICKPARAGRAPH, false),
        orderNote = get(PreferencesType.ORDERNOTE, false),
        darkModeOn = get(PreferencesType.DARKMODEON, false)
    )
}
