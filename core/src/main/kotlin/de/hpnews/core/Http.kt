package de.hpnews.core

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class Http(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    fun get(url: String): Pair<String, String> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) HPNewsReader/1.0")
            .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} für $url")
            // Rückgabe: (endgültige URL nach Weiterleitungen, Inhalt)
            return response.request.url.toString() to (response.body?.string() ?: "")
        }
    }
}
