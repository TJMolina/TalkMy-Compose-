package com.example.talkmy.domain.usecases.preferences
import com.example.talkmy.domain.repositories.AllUserPreferencesRepository
import javax.inject.Inject

/**
 * Use Case to retrieve and observe all app preferences reactively.
 */
class GetPreferences @Inject constructor(
    private val allUserPreferencesRepository: AllUserPreferencesRepository
) {
    operator fun invoke() = allUserPreferencesRepository.loadAll()
}
