package de.hpnews.core

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class FeedResult(
    val items: List<NewsItem>,
    /** Quellen, die gerade nicht erreichbar waren (Name → Fehler). */
    val failures: Map<String, String>,
)

class NewsRepository(
    private val sources: List<Source> = Sources.default,
    private val http: Http = Http(),
) {
    /** Lädt alle Feeds parallel, entfernt Dubletten und sortiert nach Datum (neueste zuerst). */
    fun loadAll(): FeedResult {
        val pool = Executors.newFixedThreadPool(minOf(6, sources.size.coerceAtLeast(1)))
        try {
            val futures = sources.map { src -> src to pool.submit<List<NewsItem>> { FeedParser.parse(http.get(src.feedUrl).second, src) } }
            val failures = mutableMapOf<String, String>()
            val all = futures.flatMap { (src, f) ->
                try {
                    f.get(30, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    failures[src.name] = (e.cause ?: e).message ?: e.toString()
                    emptyList()
                }
            }
            return FeedResult(dedupe(all), failures)
        } finally {
            pool.shutdownNow()
        }
    }

    companion object {
        fun normalizeTitle(t: String) = t.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

        fun dedupe(items: List<NewsItem>): List<NewsItem> {
            val seenLinks = HashSet<String>()
            val seenTitles = HashSet<String>()
            val out = ArrayList<NewsItem>()
            // Direkte Quellen (mit Volltext) zuerst, damit sie bei Dubletten gewinnen.
            for (item in items.sortedBy { if (it.feedContentHtml != null) 0 else 1 }) {
                val linkKey = item.link.substringBefore('?').trimEnd('/')
                val titleKey = normalizeTitle(item.title)
                if (!seenLinks.add(linkKey) || !seenTitles.add(titleKey)) {
                    // Dublette: Themen zusammenführen (z. B. HP-Artikel, der auch in der Emma-Suche auftaucht)
                    val i = out.indexOfFirst { it.link.substringBefore('?').trimEnd('/') == linkKey || normalizeTitle(it.title) == titleKey }
                    if (i >= 0) out[i] = out[i].copy(
                        isHarryPotter = out[i].isHarryPotter || item.isHarryPotter,
                        isEmmaWatson = out[i].isEmmaWatson || item.isEmmaWatson,
                        isHboSeries = out[i].isHboSeries || item.isHboSeries,
                    )
                    continue
                }
                out += item
            }
            return out.sortedByDescending { it.published ?: Instant.EPOCH }
        }
    }
}
