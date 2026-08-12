package com.example.talkmy.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.*
import com.example.talkmy.data.network.utils.CookiesUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrl
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

    private val blacklistedExtensions = setOf(
        "jpg", "png", "webp", "mpg", "mpeg", "jpeg", "webm",
        "mp4", "mp3", "gifv", "flv", "asf", "mov", "mng",
        "mkv", "ogg", "avi", "wav", "woff2", "woff", "ttf",
        "css", "vtt", "srt", "ts", "gif"
    )

    companion object {
        var webViewUserAgent: String? = null
        val capturedHeaders = ConcurrentHashMap<String, Map<String, String>>()
        val CONTENT_TYPE_REGEX = Regex("""(.*);(?:.*charset=(.*)(?:|;)|)""")
    }

    private fun isBlockedTrackerUrl(url: String): Boolean {
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return false
        return blockedTrackerHosts.any { blocked ->
            host == blocked || host.endsWith(".$blocked")
        }
    }

    suspend fun getWebViewUserAgent(): String? {
        return webViewUserAgent ?: withContext(Dispatchers.Main) {
            WebView(context).settings.userAgentString.also { userAgent ->
                webViewUserAgent = userAgent
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return runBlocking {
            val result = resolveUsingWebView(request)
            val fixedRequest = result.first as? Request
            return@runBlocking chain.proceed(fixedRequest ?: request)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolveUsingWebView(
        request: Request,
        interceptUrl: Regex? = null,
        additionalUrls: List<Regex> = emptyList(),
        userAgent: String? = null,
        useOkhttp: Boolean = false,
        scriptToFinish: String? = null,
        requestCallBack: (Request) -> Boolean = { false }
    ): Pair<Any?, List<Request>> {
        val url = request.url.toString()
        val headers = request.headers
        
        val deferredResponse = CompletableDeferred<Pair<Any?, List<Request>>>()
        val extraRequestList = mutableListOf<Request>()
        var fixedRequest: Request? = null
        var extractedResult: String? = null

        withContext(Dispatchers.Main) {
            val webView = WebView(context)
            
            fun destroyWebView() {
                webView.stopLoading()
                webView.destroy()
            }

            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                if (userAgent != null) {
                    userAgentString = userAgent
                }
            }

            webViewUserAgent = webView.settings.userAgentString
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

            class MyJavaScriptInterface {
                @JavascriptInterface
                fun onElementFound(html: String) {
                    if (html.isNotEmpty()) {
                        extractedResult = html
                        deferredResponse.complete(extractedResult to extraRequestList)
                    }
                }
            }
            webView.addJavascriptInterface(MyJavaScriptInterface(), "NativeAndroid")

            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val webViewUrl = request.url.toString()
                    if (isBlockedTrackerUrl(webViewUrl)) {
                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                    }

                    if (interceptUrl?.containsMatchIn(webViewUrl) == true) {
                        fixedRequest = request.toRequest().also { requestCallBack(it) }
                        val host = runCatching { URI(webViewUrl).host }.getOrNull() ?: ""
                        if (!webViewUrl.contains("/cdn-cgi/") && !webViewUrl.contains("cloudflare")) {
                            capturedHeaders[host] = request.requestHeaders
                        }
                        return null
                    }

                    if (additionalUrls.any { it.containsMatchIn(webViewUrl) }) {
                        val req = request.toRequest()
                        extraRequestList.add(req)
                        val host = runCatching { URI(webViewUrl).host }.getOrNull() ?: ""
                        if (!webViewUrl.contains("/cdn-cgi/") && !webViewUrl.contains("cloudflare")) {
                            capturedHeaders[host] = request.requestHeaders
                        }
                        if (requestCallBack(req)) {
                            deferredResponse.complete(fixedRequest to extraRequestList)
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, finishUrl: String?) {
                    super.onPageFinished(view, finishUrl)
                    if (finishUrl == null) return

                    val isChallengeSolved = CookiesUtils.getAllCookiesForUrl(finishUrl).containsKey("cf_clearance")

                    if (isChallengeSolved) {
                        CookieManager.getInstance().flush()
                        if (scriptToFinish == null) {
                            @OptIn(DelicateCoroutinesApi::class)
                            GlobalScope.launch(Dispatchers.Main) {
                                delay(2000L)
                                if (requestCallBack(Request.Builder().url(finishUrl).build())) {
                                    deferredResponse.complete(fixedRequest to extraRequestList)
                                }
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
    return Request.Builder()
        .url(this.url.toString())
        .method(this.method, null)
        .headers(this.requestHeaders.toHeaders())
        .build()
}

fun Map<String, String>.toHeaders(): okhttp3.Headers {
    val builder = okhttp3.Headers.Builder()
    this.forEach { (k, v) -> builder.add(k, v) }
    return builder.build()
}
