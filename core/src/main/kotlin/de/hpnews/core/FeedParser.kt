package de.hpnews.core

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Liest RSS 2.0 und Atom. */
object FeedParser {

    fun parse(xml: String, source: Source): List<NewsItem> {
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val entries = doc.select("item").ifEmpty { doc.select("entry") }
        return entries.mapNotNull { parseEntry(it, source) }
    }

    private fun Element.childText(vararg tags: String): String? {
        for (t in tags) {
            val el = this.children().firstOrNull { it.tagName().equals(t, ignoreCase = true) }
            val txt = el?.text()?.trim()
            if (!txt.isNullOrEmpty()) return txt
        }
        return null
    }

    private fun parseEntry(e: Element, source: Source): NewsItem? {
        var title = e.childText("title")?.let { htmlToText(it) } ?: return null
        val rawLink = e.childText("link")
            ?: e.children().firstOrNull { it.tagName() == "link" }?.attr("href")?.takeIf { it.isNotBlank() }
            ?: return null
        val link = unwrapAggregatorLink(rawLink)

        val description = e.childText("description", "summary") ?: ""
        val content = e.childText("content:encoded", "content")
        var teaser = htmlToText(description).take(600)

        val publisher = if (source.isAggregator) {
            e.childText("News:Source", "news:source", "source") ?: hostOf(link)
        } else source.name

        if (source.isAggregator) {
            // Google News hängt " - Herausgeber" an den Titel und wiederholt ihn als Beschreibung.
            title = title.removeSuffix(" - $publisher").trim()
            if (teaser.startsWith(title) || teaser.removeSuffix(publisher).trim() == title) teaser = ""
        }

        val published = e.childText("pubDate", "published", "updated", "dc:date")?.let { parseDate(it) }

        val language = source.language ?: LanguageDetector.detect("$title $teaser")
        val topicText = "$title $teaser"

        val isHp = !source.isEmmaWatsonSearch && (!source.filterTopic || Sources.isOnTopic(topicText)) ||
            source.isEmmaWatsonSearch && Sources.isOnTopic(topicText)
        val isEmma = source.isEmmaWatsonSearch || Sources.mentionsEmmaWatson(topicText)
        // Suchen und allgemeine Film-/Serienseiten: nur passende Themen behalten.
        if (!isHp && !isEmma) return null

        return NewsItem(
            id = sha1(link),
            title = title,
            link = link,
            publisher = publisher,
            foundVia = source.name,
            published = published,
            teaser = teaser,
            feedContentHtml = content,
            language = language,
            isHarryPotter = isHp,
            isEmmaWatson = isEmma,
            isHboSeries = isHp && Sources.looksLikeHboSeries(topicText + " " + (content?.let { htmlToText(it).take(2000) } ?: "")),
        )
    }

    /** Bing verpackt Links als .../apiclick.aspx?...&url=<echte URL>&... */
    fun unwrapAggregatorLink(link: String): String {
        return try {
            val uri = URI(link)
            if (uri.host?.contains("bing.com") == true) {
                val q = uri.rawQuery ?: return link
                q.split('&').map { it.split('=', limit = 2) }
                    .firstOrNull { it[0] == "url" && it.size == 2 }
                    ?.let { URLDecoder.decode(it[1], "UTF-8") } ?: link
            } else link
        } catch (_: Exception) {
            link
        }
    }

    fun hostOf(link: String): String = try {
        URI(link).host?.removePrefix("www.") ?: link
    } catch (_: Exception) {
        link
    }

    fun htmlToText(html: String): String = Jsoup.parse(html).text().trim()

    private fun parseDate(s: String): Instant? {
        val t = s.trim()
        return runCatching { ZonedDateTime.parse(t, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(t).toInstant() }.getOrNull()
            ?: runCatching { Instant.parse(t) }.getOrNull()
            ?: runCatching {
                // Bing: "Sat, 07 Nov 2026 08:15:00 GMT" mit einstelligem Tag u. ä.
                ZonedDateTime.parse(t, DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH)).toInstant()
            }.getOrNull()
    }

    fun sha1(s: String): String =
        MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
