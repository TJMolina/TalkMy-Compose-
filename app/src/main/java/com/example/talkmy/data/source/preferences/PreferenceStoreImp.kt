package com.example.talkmy.data.source.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.talkmy.domain.models.PreferenceData
import com.example.talkmy.domain.interfaces.PreferenceStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class PreferenceStoreImp(
    context: Context,
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("talkmy_prefs", Context.MODE_PRIVATE),
) : PreferenceStore {

    private val keyFlow = sharedPreferences.keyFlow

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, defaultValue: T): PreferenceData<T> {
        return when (defaultValue) {
            is String -> getString(key, defaultValue) as PreferenceData<T>
            is Int -> getInt(key, defaultValue) as PreferenceData<T>
            is Boolean -> getBoolean(key, defaultValue) as PreferenceData<T>
            is Float -> getFloat(key, defaultValue) as PreferenceData<T>
            is Long -> getLong(key, defaultValue) as PreferenceData<T>
            else -> throw IllegalArgumentException("Unsupported type for preference: ${defaultValue!!::class.simpleName}")
        }
    }

    override fun getString(key: String, defaultValue: String): PreferenceData<String> {
        return AndroidPreference.StringPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): PreferenceData<Long> {
        return AndroidPreference.LongPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): PreferenceData<Int> {
        return AndroidPreference.IntPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): PreferenceData<Float> {
        return AndroidPreference.FloatPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): PreferenceData<Boolean> {
        return AndroidPreference.BooleanPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): PreferenceData<Set<String>> {
        return AndroidPreference.StringSetPrimitive(sharedPreferences, keyFlow, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): PreferenceData<T> {
        return AndroidPreference.Object(
            preferences = sharedPreferences,
            keyFlow = keyFlow,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override fun getAll(): Map<String, *> {
        return sharedPreferences.all ?: emptyMap<String, Any>()
    }
}

private val SharedPreferences.keyFlow
    get() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
            trySend(
                key,
            )
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }