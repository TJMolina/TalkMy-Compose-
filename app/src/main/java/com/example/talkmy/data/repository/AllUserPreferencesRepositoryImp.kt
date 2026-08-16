package com.example.talkmy.data.repository
import com.example.talkmy.domain.interfaces.PreferenceStore
import com.example.talkmy.domain.models.UserPreference
import com.example.talkmy.domain.repositories.AllUserPreferencesRepository
import javax.inject.Inject

class AllUserPreferencesRepositoryImp @Inject constructor(
    val preferenceStore: PreferenceStore
): AllUserPreferencesRepository {
    override fun set(type: UserPreference<*>) {
        val key = type::class.simpleName ?: return
        val value = type.value
        preferenceStore.get(key, value).set(value)
    }

    override fun loadAll(): List<UserPreference<*>> {
        return UserPreference.allDefaults.map { default ->
            val key = default::class.simpleName!!

            @Suppress("UNCHECKED_CAST")
            val pref = default as UserPreference<Any>
            pref.value = preferenceStore.get(key, pref.value).get()
            pref
        }
    }
}