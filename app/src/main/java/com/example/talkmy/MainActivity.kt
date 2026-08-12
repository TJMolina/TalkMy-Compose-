package com.example.talkmy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.talkmy.ui.components.WebViewManager
import com.example.talkmy.ui.navigation.AppNavigation
import com.example.talkmy.ui.theme.TalkMyTheme
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var webViewManager: WebViewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.post {
            webViewManager.preload()
        }
        setContent {
            TalkMyTheme {
                AppNavigation(webViewManager = webViewManager)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TalkMyTheme {
        // Preview can't easily inject, so we pass null or a mock if we want
        AppNavigation(webViewManager = WebViewManager(LocalContext.current))
    }
}
