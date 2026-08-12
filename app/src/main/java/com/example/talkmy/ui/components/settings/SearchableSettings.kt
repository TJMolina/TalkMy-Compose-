package com.example.talkmy.ui.components.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.example.talkmy.domain.models.Preference
import com.example.talkmy.ui.core.ComposableScreen


interface SearchableSettings : ComposableScreen {

    @Composable
    @ReadOnlyComposable
    fun getTitleRes(): String

    @Composable
    fun getPreferences(): List<Preference>

    @Composable
    fun RowScope.AppBarAction() {
    }

    @Composable
    override fun Content() {
        PreferenceScaffold(
            titleRes = getTitleRes(),
            onBackPressed = null,
            actions = { AppBarAction() },
            itemsProvider = { getPreferences() },
        )
    }

    companion object {
        // HACK: for the background blipping thingy.
        // The title of the target PreferenceItem
        // Set before showing the destination screen and reset after
        // See BasePreferenceWidget.highlightBackground
        var highlightKey: String? = null
    }
}