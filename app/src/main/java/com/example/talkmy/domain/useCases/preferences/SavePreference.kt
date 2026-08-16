package com.example.talkmy.domain.usecases.preferences

import com.example.talkmy.domain.models.UserPreference
import com.example.talkmy.domain.repositories.AllUserPreferencesRepository
import javax.inject.Inject

/**
 * Generic Use Case to persist any app preference.
 */
class SavePreference @Inject constructor(
    private val allUserPreferencesRepository: AllUserPreferencesRepository
) {
    operator fun invoke(type: UserPreference<*>) {
        allUserPreferencesRepository.set(type)
    }
}
