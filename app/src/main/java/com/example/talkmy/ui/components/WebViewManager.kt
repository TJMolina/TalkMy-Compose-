package com.example.talkmy.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.ref.WeakReference

object WebViewManager {
    private var webView: WeakReference<WebView>? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun preload(context: Context) {
        if (webView == null) {
            webView = WeakReference(WebView(context.applicationContext).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl("https://www.google.com")
            })
        }
    }

    fun getWebView(context: Context): WebView {
        if (webView == null) {
            preload(context)
        }
        
        val instance = webView?.get()!!
        
        // Es vital remover el WebView de cualquier padre anterior antes de reusarlo
        (instance.parent as? ViewGroup)?.removeView(instance)
        
        return instance
    }
}
