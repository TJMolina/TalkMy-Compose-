package com.example.talkmy.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.lagradost.nicehttp.requestCreator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * An OkHttp interceptor that uses a hidden WebView to solve Cloudflare challenges.
 * It also supports extracting dynamic content through injected scripts.
 */
@Singleton
class WebViewResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    private val blockedTrackerHosts = setOf(
        "google-analytics.com",
        "googletagmanager.com",
        "googlesyndication.com",
        "doubleclick.net",
        "adtrafficquality.google",
        "sharethis.com",
        "count-server.sharethis.com",
        "fundingchoicesmessages.google.com"
    )

    /** Common binary/asset extensions to block in the WebView to save bandwidth and speed up bypass. */
    private val blacklistedExtensions = setOf(
        "jpg", "png", "webp", "mpg", "mpeg", "jpeg", "webm",
        "mp4", "mp3", "gifv", "flv", "asf", "mov", "mng",
        "mkv", "ogg", "avi", "wav", "woff2", "woff", "ttf",
        "css", "vtt", "srt", "ts", "gif"
    )

    companion object {
        /** Cache for the system WebView's default User-Agent. */
        var webViewUserAgent: String? = null
        /** Global map to store high-fidelity headers (like sec-ch-ua) captured from the WebView. */
        val capturedHeaders = ConcurrentHashMap<String, Map<String, String>>()
        /** Regex to parse Content-Type and Charset from HTTP headers. */
        val CONTENT_TYPE_REGEX = Regex("""(.*);(?:.*charset=(.*)(?:|;)|)""")
    }

    /** Utility to check if a URL belongs to a blocked tracker host. */
    private fun isBlockedTrackerUrl(url: String): Boolean {
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return false
        return blockedTrackerHosts.any { blocked ->
            host == blocked || host.endsWith(".$blocked")
        }
    }

    /** Lazily retrieves and caches the default User-Agent from a dummy WebView. */
    suspend fun getWebViewUserAgent(): String? {
        return webViewUserAgent ?: withContext(Dispatchers.Main) {
            WebView(context).settings.userAgentString.also { userAgent ->
                webViewUserAgent = userAgent
            }
        }
    }

    /**
     * Standard OkHttp Interceptor implementation.
     * When a request is intercepted, it tries to "resolve" it using the hidden WebView.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return runBlocking {
            // resolveUsingWebView returns the final request after the bypass
            val fixedRequest = resolveUsingWebView(request).first as? Request
            return@runBlocking chain.proceed(fixedRequest ?: request)
        }
    }

    /**
     * Resolves the Cloudflare challenge and optionally extracts content.
     * @param requestCallBack asynchronously return matched requests by either interceptUrl or additionalUrls. If true, destroy WebView.
     * @return the final request (by interceptUrl) and all the collected urls (by additionalUrls), or the extracted script String.
     * */
    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolveUsingWebView(
        request: Request,
        interceptUrl: Regex? = null,
        additionalUrls: List<Regex> = emptyList(),
        userAgent: String? = null,
        scriptToFinish: String? = null,
        requestCallBack: (Request) -> Boolean = { false }
    ): Pair<Any?, List<Request>> {
        val url = request.url.toString()
        val headers = request.headers

        // We use a Deferred to wait for the WebView to signal completion (success or timeout)
        val deferredResponse = CompletableDeferred<Pair<Any?, List<Request>>>()
        val extraRequestList = mutableListOf<Request>()
        var fixedRequest: Request? = null
        var extractedResult: String? = null
        /** Reference to a delayed job used to wait for cookie rotation/stability before closing. */
        var stabilityJob: kotlinx.coroutines.Job? = null

        class MyJavaScriptInterface {
            @JavascriptInterface
            fun onElementFound(html: String) {
                if (html.isNotEmpty()) {
                    extractedResult = html
                    deferredResponse.complete(extractedResult to extraRequestList)
                }
            }
        }

        withContext(Dispatchers.Main) {
            val webView = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    if (userAgent != null) {
                        userAgentString = userAgent
                    }
                }
                addJavascriptInterface(MyJavaScriptInterface(), "NativeAndroid")
            }

            fun destroyWebView() {
                CoroutineScope(Dispatchers.Main).launch {
                    stabilityJob?.cancel()
                    webView.stopLoading()
                    webView.destroy()
                }
            }


            webViewUserAgent = webView.settings.userAgentString
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)


            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val webViewUrl = request.url.toString()
                    if (isBlockedTrackerUrl(webViewUrl)) {
                        return WebResourceResponse(
                            "text/plain",
                            "utf-8",
                            ByteArrayInputStream(ByteArray(0))
                        )
                    }

                    val req = request.toRequest()

                    if (!webViewUrl.contains("/cdn-cgi/") && !webViewUrl.contains("cloudflare")) {
                        capturedHeaders[runCatching { URI(webViewUrl).host }.getOrNull() ?: ""] = request.requestHeaders
                    }

                    // Check if this request matches our target URL
                    if (interceptUrl?.containsMatchIn(webViewUrl) == true) {
                        fixedRequest = req
                        deferredResponse.complete(req to extraRequestList)
                        return null
                    }

                    // Track additional interesting URLs
                    if (additionalUrls.any { it.containsMatchIn(webViewUrl) }) {
                        extraRequestList.add(req)
                        // If callback returns true (e.g., "I found what I wanted"), signal completion
                        if (requestCallBack(req)) {
                            deferredResponse.complete(fixedRequest to extraRequestList)
                        }
                    }

                    val path = runCatching { URI(webViewUrl).path }.getOrNull() ?: ""
                    val extension = path.substringAfterLast('.', "").lowercase()
                    // Optionally route WebView requests through OkHttp to sync cookies/state
                    return try {
                        when {
                            blacklistedExtensions.contains(extension) ||
                            webViewUrl.endsWith("/favicon.ico") ||
                            webViewUrl.startsWith("wss://") -> WebResourceResponse("image/png", null, null)
                            webViewUrl.contains("recaptcha") ||
                            webViewUrl.contains("/cdn-cgi/") -> super.shouldInterceptRequest(view, request)
                            else -> super.shouldInterceptRequest(view, request)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                override fun onPageFinished(view: WebView?, finishUrl: String?) {
                    super.onPageFinished(view, finishUrl)
                    if (finishUrl == null) return

                    if (requestCallBack(requestCreator("GET", finishUrl))) {
                        if (scriptToFinish == null) {
                            stabilityJob?.cancel()
                            stabilityJob = CoroutineScope(Dispatchers.Main).launch {
                                delay(5.seconds)
                                deferredResponse.complete(fixedRequest to extraRequestList)
                            }
                        }
                    }


                    val script = scriptToFinish ?: """
                        (function() {
                            if (window.wasClicked) return;
                            function tryClick() {
                                var isCloudflarePage = document.querySelector('#challenge-form') || 
                                                       document.querySelector('#challenge-running') ||
                                                       document.querySelector('#cf-challenge-running');
                                if (!isCloudflarePage) return; 
                                var cfToken = document.querySelector('[name="cf-turnstile-response"]')?.value 
                                              || document.querySelector('#cf-chl-widget-multi-token')?.value;
                                var submitButton = document.querySelector('#challenge-form button[type="submit"]') 
                                                   || document.querySelector('#challenge-form input[type="submit"]');
                                if (cfToken && submitButton) {
                                    window.wasClicked = true;
                                    submitButton.click();
                                } else {
                                    if (!window.retryCount) window.retryCount = 0;
                                    if (window.retryCount < 15) { 
                                        window.retryCount++;
                                        setTimeout(tryClick, 1000);
                                    }
                                }
                            }
                            tryClick();
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(script, null)
                }
            }
            webView.loadUrl(url, headers.toMap())

            deferredResponse.invokeOnCompletion { destroyWebView() }
        }

        return withTimeoutOrNull(60.seconds) {
            deferredResponse.await()
        } ?: (extractedResult to extraRequestList)
    }
}

fun WebResourceRequest.toRequest(): Request {
    return requestCreator(
        this.method,
        this.url.toString(),
        this.requestHeaders,
    )
}