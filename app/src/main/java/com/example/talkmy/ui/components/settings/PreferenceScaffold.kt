package com.example.talkmy.ui.components.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.talkmy.domain.models.Preference

@Composable
fun PreferenceScaffold(
    titleRes: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    Scaffold(
        /*topBar = {
            AppBar(
                title = titleRes,
                navigateUp = onBackPressed,
                actions = actions,
                scrollBehavior = it,
            )
        },*/
        content = { contentPadding ->
            PreferenceScreen(
                items = itemsProvider(),
                contentPadding = contentPadding,
            )
        },
    )
}