package com.example.talkmy.data.network

import android.util.Log
import android.webkit.CookieManager
import androidx.annotation.AnyThread
import com.example.talkmy.data.network.utils.CookiesUtils.clearCookiesForHost
import com.example.talkmy.data.network.utils.CookiesUtils.getAllCookiesForUrl
import com.example.talkmy.data.network.utils.CookiesUtils.toCookieString
import com.lagradost.nicehttp.Requests.Companion.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import java.util.concurrent.ConcurrentHashMap
import com.example.talkmy.di.BaseClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Interceptor designed to bypass Cloudflare and Turnstile protections.
 * It detects challenges and uses a hidden WebView to solve them and capture the resulting session.
 */
@Singleton
class CloudflareKiller @Inject constructor(
    private val webViewResolver: WebViewResolver,
    @BaseClient private val okHttpClientProvider: Provider<OkHttpClient>
) : Interceptor {
    companion object {
        const val TAG = "CloudflareKiller"
        private val mutex = Mutex() // Ensures only one bypass runs at a time
    }

    val savedCookies = ConcurrentHashMap<String, Map<String, String>>()

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        val request = chain.request()
        val url = request.url.toString()
        val host = request.url.host

        // If we already have cookies in CookieManager, use them
        val initialCookies = savedCookies[host] ?: getAllCookiesForUrl(url)
        if (initialCookies.containsKey("cf_clearance")) {
            val response = proceed(request, initialCookies)
            if (!looksLikeCloudflareChallenge(response)) {
                return@runBlocking response
            }
            // If saved cookies trigger a challenge, they are expired/invalid
            response.close()
            clearCookiesForHost(request.url)
            savedCookies.remove(host)
        }

        // Try the request normally
        val initialResponse = chain.proceed(request)
        if (!looksLikeCloudflareChallenge(initialResponse)) {
            return@runBlocking initialResponse
        }
        initialResponse.close()

        // Bypass needed. Locked section to prevent multiple WebViews from opening
        mutex.withLock {
            val currentCookies = savedCookies[host] ?: getAllCookiesForUrl(url)
            if (currentCookies.containsKey("cf_clearance")) {
                val response = proceed(request, currentCookies)
                if (!looksLikeCloudflareChallenge(response)) return@runBlocking response
                response.close()
                clearCookiesForHost(request.url)
                savedCookies.remove(host)
            }

            Log.d(TAG, "Resolving Cloudflare for $host...")
            val bypassResponse = bypassCloudflare(request)

            if (bypassResponse != null) {
                if (!looksLikeCloudflareChallenge(bypassResponse)) {
                    Log.d(TAG, "Succeeded bypassing cloudflare: ${request.url}")
                    return@runBlocking bypassResponse
                }
                bypassResponse.close()
            }
        }

        return@runBlocking chain.proceed(request)
    }

    private fun looksLikeCloudflareChallenge(response: Response): Boolean {
        val hasCloudflareHeaders =
            response.header("cf-ray") != null ||
                    response.header("server")?.contains("cloudflare", ignoreCase = true) == true

        // Read a small sample of the body to check for challenge scripts
        val bodySample = runCatching {
            response.peekBody(1024 * 10).string().lowercase()
        }.getOrDefault("")

        val isChallengeBody = bodySample.contains("cf-browser-verification") ||
                bodySample.contains("checking your browser") ||
                bodySample.contains("just a moment") ||
                bodySample.contains("/cdn-cgi/")

        if (response.code in listOf(403, 429, 503) || (response.code == 200 && bodySample.contains("one moment"))) {
            if (hasCloudflareHeaders || isChallengeBody) return true
        }

        return response.header("location").orEmpty().lowercase().contains("/cdn-cgi/") || isChallengeBody
    }

    /**
     * Reconstructs the request to mirror a real browser's identity
     */
    private suspend fun proceed(request: Request, cookiesMap: Map<String, String>): Response {
        val host = request.url.host
        val ua = WebViewResolver.webViewUserAgent ?: webViewResolver.getWebViewUserAgent() ?: USER_AGENT
        val captured = WebViewResolver.capturedHeaders[host] ?: emptyMap()

        val builder = Headers.Builder()
        builder.add("Host", host)
        captured.filter { it.key.lowercase().startsWith("sec-ch-ua") }.forEach { (k, v) ->
            val masked = v.replace(", \"Android WebView\";v=\"150\"", "").replace("Android WebView", "Chromium")
            builder.add(k, masked)
        }
        builder.add("User-Agent", ua)
        builder.add("Accept", captured["Accept"] ?: "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        builder.add("Origin", "${request.url.scheme}://${request.url.host}")

        captured.filter { it.key.lowercase().startsWith("sec-fetch-") }.forEach { (k, v) ->
            builder.add(k, v)
        }
        val referer = request.header("Referer") ?: captured["Referer"] ?: "${request.url.scheme}://${request.url.host}/"
        builder.add("Referer", referer)
        builder.add("Accept-Language", captured["Accept-Language"] ?: "en-US,en;q=0.9")
        
        val finalCookies = cookiesMap + request.headers.values("Cookie").associate {
            val split = it.split("=")
            (split.getOrNull(0) ?: "") to (split.getOrNull(1) ?: "")
        }.filter { it.key.isNotBlank() }

        builder.add("Cookie", finalCookies.toCookieString())
        
        val usedKeys = builder.build().names()
        request.headers.forEach { (k, v) ->
            if (!usedKeys.contains(k)) {
                builder.add(k, v)
            }
        }

        // Use the provider to get the base client without THIS interceptor to avoid infinite loop
        return okHttpClientProvider.get().newCall(
            request.newBuilder()
                .headers(builder.build())
                .build()
        ).await()
    }

    /** Invokes the WebView to solve the challenge and extract new cookies */
    private suspend fun bypassCloudflare(request: Request): Response? {
        val url = request.url.toString()
        val host = request.url.host

        webViewResolver.resolveUsingWebView(
            request = request,
            interceptUrl = Regex(".^"),
            additionalUrls = listOf(Regex(".")),
            requestCallBack = {
                val cookie = CookieManager.getInstance().getCookie(it.url.toString())
                cookie?.contains("cf_clearance") == true
            }
        )

        val cookies = getAllCookiesForUrl(url)
        if (cookies.containsKey("cf_clearance")) {
            savedCookies[host] = cookies
            return proceed(request, cookies)
        }
        return null
    }
}