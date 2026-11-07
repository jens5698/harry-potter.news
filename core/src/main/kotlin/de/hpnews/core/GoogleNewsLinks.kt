package de.hpnews.core

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Google News verlinkt nicht direkt auf den Artikel, sondern auf news.google.com/rss/articles/<id>.
 * Die echte Adresse liefert Google über eine interne Schnittstelle (wie es auch die Webseite tut).
 * Klappt das nicht, bleibt der Google-Link – der funktioniert im Browser trotzdem.
 */
object GoogleNewsLinks {
    private val mapper = ObjectMapper()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private const val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36"

    fun isGoogleNews(link: String) = runCatching { URI(link).host == "news.google.com" }.getOrDefault(false)

    fun resolve(link: String): String? {
        if (!isGoogleNews(link)) return link
        val id = URI(link).path.substringAfterLast('/').takeIf { it.isNotBlank() } ?: return null

        val page = client.newCall(
            Request.Builder().url("https://news.google.com/rss/articles/$id").header("User-Agent", UA).build(),
        ).execute().use { it.body?.string() } ?: return null
        val el = Jsoup.parse(page).selectFirst("[data-n-a-sg][data-n-a-ts]") ?: return null
        val sg = el.attr("data-n-a-sg")
        val ts = el.attr("data-n-a-ts")

        val inner = """["garturlreq",[["X","X",["X","X"],null,null,1,1,"US:en",null,1,null,null,null,null,null,0,1],"X","X",1,[1,1,1],1,1,null,0,0,null,0],"$id",$ts,"$sg"]"""
        val fReq = mapper.writeValueAsString(listOf(listOf(listOf("Fbv4je", inner, null, "generic"))))
        val body = FormBody.Builder().add("f.req", fReq).build()
        val resp = client.newCall(
            Request.Builder()
                .url("https://news.google.com/_/DotsSplashUi/data/batchexecute")
                .header("User-Agent", UA)
                .post(body)
                .build(),
        ).execute().use { it.body?.string() } ?: return null

        // Antwort: ")]}'\n\n[...]" – darin ein JSON-String mit ["garturlres","<url>",...]
        val jsonPart = resp.substringAfter("\n\n", "").ifBlank { resp.substringAfter("\n") }
        val outer = mapper.readTree(jsonPart)
        val payload = outer.firstOrNull { it.isArray && it.size() > 2 && it[1].asText() == "Fbv4je" }?.get(2)?.asText()
            ?: return null
        val url = mapper.readTree(payload).get(1)?.asText()
        return url?.takeIf { it.startsWith("http") }
    }
}
