package de.hpnews.core

import java.time.Instant

enum class Lang { DE, EN, OTHER }

/** Eine Nachrichtenquelle (RSS- oder Atom-Feed). */
data class Source(
    val name: String,
    val feedUrl: String,
    /** Sprache der Quelle, falls bekannt. null = automatisch erkennen. */
    val language: Lang? = null,
    /** Bei Suchfeeds (Bing News) steht der echte Herausgeber in jedem Eintrag. */
    val isAggregator: Boolean = false,
    /** true = nur Einträge behalten, die wirklich mit Harry Potter zu tun haben. */
    val filterTopic: Boolean = isAggregator,
)

/** Ein Eintrag aus einem Feed. */
data class NewsItem(
    val id: String,
    val title: String,
    val link: String,
    /** Wo der Artikel ursprünglich erschienen ist (z. B. "MuggleNet", "Spiegel"). */
    val publisher: String,
    /** Über welchen Feed wir ihn gefunden haben. */
    val foundVia: String,
    val published: Instant?,
    val teaser: String,
    /** Volltext aus dem Feed (content:encoded), falls die Quelle ihn mitliefert. */
    val feedContentHtml: String?,
    val language: Lang,
    val isHboSeries: Boolean,
)

/** Der lesbare Text eines Artikels. */
data class ArticleText(
    val paragraphs: List<String>,
    /** false, wenn nur ein Anriss verfügbar war (z. B. Paywall oder Seite nicht lesbar). */
    val isComplete: Boolean,
    /** Echte Adresse des Artikels (bei Google-News-Links aufgelöst), falls bekannt. */
    val resolvedUrl: String? = null,
)

data class VocabEntry(
    val english: String,
    val german: String,
)

/** Ergebnis der KI: Artikel in einfachem Englisch plus Vokabelliste. */
data class SimplifiedArticle(
    val title: String,
    val paragraphs: List<String>,
    val vocabulary: List<VocabEntry>,
)
