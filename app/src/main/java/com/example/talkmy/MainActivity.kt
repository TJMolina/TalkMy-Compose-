package com.example.talkmy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.talkmy.ui.components.WebViewManager
import com.example.talkmy.ui.navigation.AppNavigation
import com.example.talkmy.ui.theme.TalkMyTheme
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Logger.d("Se encendio logger")
        window.decorView.post {
            WebViewManager.preload(this@MainActivity)
        }
        setContent {
            TalkMyTheme {
                AppNavigation()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TalkMyTheme {
        AppNavigation()
    }
}
