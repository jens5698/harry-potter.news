package de.hpnews.core

import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/**
 * Diagnose: prüft alle Quellen (und Kandidaten) und gibt aus, was ankommt.
 * Aufruf: ./gradlew -PcoreOnly :core:checkFeeds
 */
fun main() {
    val http = Http()
    val candidates = Sources.default + Sources.candidates
    for (src in candidates) {
        println("=== ${src.name}\n    ${src.feedUrl}")
        try {
            val (finalUrl, body) = http.get(src.feedUrl)
            val doc = Jsoup.parse(body, "", Parser.xmlParser())
            val raw = doc.select("item").ifEmpty { doc.select("entry") }.size
            val items = FeedParser.parse(body, src)
            println("    final=$finalUrl bytes=${body.length} rohEinträge=$raw nachFilter=${items.size}")
            if (raw == 0) println("    Anfang: " + body.take(300).replace(Regex("\\s+"), " "))
            items.take(3).forEach { println("    - [${it.publisher}] ${it.title} -> ${it.link}") }
        } catch (e: Exception) {
            println("    FEHLER: ${e.message ?: e}")
        }
    }
}
