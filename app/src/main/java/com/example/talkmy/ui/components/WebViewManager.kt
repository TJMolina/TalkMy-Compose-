package com.example.talkmy.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var webViewRef: WeakReference<WebView>? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun preload() {
        if (webViewRef?.get() == null) {
            webViewRef = WeakReference(WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl("https://www.google.com")
            })
        }
    }

    fun getWebView(requestContext: Context): WebView {
        var instance = webViewRef?.get()
        if (instance == null) {
            preload()
            instance = webViewRef?.get()!!
        }

        (instance.parent as? ViewGroup)?.removeView(instance)
        
        return instance
    }
}