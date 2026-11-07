package de.hpnews.core

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Holt den lesbaren Haupttext eines Artikels – wie der "Lesemodus" eines Browsers.
 * Wenn die Quelle den Volltext schon im Feed liefert, wird der genommen.
 */
class ArticleExtractor(private val http: Http = Http()) {

    fun load(item: NewsItem): ArticleText {
        val fromFeed = item.feedContentHtml?.let { paragraphsFromHtml(it) }.orEmpty()
        if (fromFeed.joinToString(" ").length >= 600) return ArticleText(fromFeed, isComplete = true)

        var resolved: String? = null
        val fromPage = runCatching {
            val url = if (GoogleNewsLinks.isGoogleNews(item.link)) {
                runCatching { GoogleNewsLinks.resolve(item.link) }.getOrNull() ?: item.link
            } else item.link
            val (finalUrl, html) = http.get(url)
            if (!GoogleNewsLinks.isGoogleNews(finalUrl)) resolved = finalUrl
            extract(Jsoup.parse(html, finalUrl))
        }.getOrDefault(emptyList())

        if (fromPage.joinToString(" ").length >= 400) return ArticleText(fromPage, isComplete = true, resolvedUrl = resolved)

        val fallback = fromFeed.ifEmpty { listOf(item.teaser).filter { it.isNotBlank() } }
            .ifEmpty { listOf(item.title) }
        return ArticleText(fallback, isComplete = false, resolvedUrl = resolved)
    }

    fun paragraphsFromHtml(html: String): List<String> {
        val body = Jsoup.parseBodyFragment(html).body()
        val blocks = body.select("p, h2, h3, li, blockquote")
        val list = if (blocks.isEmpty()) listOf(body.text()) else blocks.map { it.text() }
        return list.map { it.trim() }.filter { it.length > 1 }.distinct()
    }

    fun extract(doc: Document): List<String> {
        doc.select("script, style, noscript, nav, header, footer, aside, form, figure figcaption, .share, .social, .related, .newsletter, .comments, [aria-hidden=true]").remove()

        val candidates = buildList {
            addAll(doc.select("article"))
            addAll(doc.select("[itemprop=articleBody], .article-body, .entry-content, .post-content, .story-body, main"))
        }.ifEmpty { listOf(doc.body()) }

        // Container mit dem meisten Absatztext wählen.
        val best = candidates.maxByOrNull { textScore(it) } ?: return emptyList()
        return best.select("p, h2, h3, li")
            .map { it.text().trim() }
            .filter { it.length >= 25 || it.endsWith(":") }
            .filterNot { boilerplate.containsMatchIn(it) }
            .distinct()
    }

    private fun textScore(el: Element): Int = el.select("p").sumOf { p ->
        val t = p.text()
        if (t.length >= 40) t.length else 0
    }

    private val boilerplate = Regex(
        """^(Werbung|Anzeige|Advertisement|Read more|Lesen Sie auch|Mehr zum Thema|Share this|Teilen|Cookie|Newsletter|Sign up|Abonnieren)\b""",
        RegexOption.IGNORE_CASE,
    )
}
