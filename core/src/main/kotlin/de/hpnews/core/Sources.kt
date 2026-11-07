package de.hpnews.core

import java.net.URLEncoder

object Sources {
    private fun googleNews(query: String, lang: String, country: String): String {
        val q = URLEncoder.encode(query, "UTF-8")
        return "https://news.google.com/rss/search?q=$q+when:30d&hl=$lang&gl=$country&ceid=$country:${lang.substringBefore('-')}"
    }

    /** Standardquellen. Lässt sich später leicht erweitern. */
    val default: List<Source> = listOf(
        // Deutschsprachig
        Source("Google News (DE)", googleNews("\"Harry Potter\"", "de", "DE"), Lang.DE, isAggregator = true),
        Source("Google News (DE): HBO-Serie", googleNews("\"Harry Potter\" Serie HBO", "de", "DE"), Lang.DE, isAggregator = true),
        Source("Filmstarts", "https://www.filmstarts.de/rss/nachrichten.xml", Lang.DE, filterTopic = true),
        Source("Moviepilot", "https://www.moviepilot.de/rss/news", Lang.DE, filterTopic = true),
        Source("Serienjunkies", "https://www.serienjunkies.de/rss/news.xml", Lang.DE, filterTopic = true),
        // Englischsprachig
        Source("Google News (EN)", googleNews("\"Harry Potter\"", "en-US", "US"), Lang.EN, isAggregator = true),
        Source("Google News (EN): HBO series", googleNews("\"Harry Potter\" HBO series", "en-US", "US"), Lang.EN, isAggregator = true),
        Source("MuggleNet", "https://www.mugglenet.com/feed/", Lang.EN),
        // Emma Watson
        Source("Google News (DE): Emma Watson", googleNews("\"Emma Watson\"", "de", "DE"), Lang.DE, isAggregator = true, isEmmaWatsonSearch = true),
        Source("Google News (EN): Emma Watson", googleNews("\"Emma Watson\"", "en-US", "US"), Lang.EN, isAggregator = true, isEmmaWatsonSearch = true),
    )

    /** Zusätzliche Kandidaten, die nur die Diagnose prüft. */
    val candidates: List<Source> = emptyList()

    private val hboPattern = Regex("""\bHBO\b|\bMax\b.*(serie|series)|(serie|series).*\bMax\b|TV[- ]?(serie|series)|Fernsehserie|Paapa Essiedu|Dominic McLaughlin|John Lithgow|Arabella Stanton|Alastair Stout""", RegexOption.IGNORE_CASE)

    fun looksLikeHboSeries(text: String): Boolean = hboPattern.containsMatchIn(text)

    private val topicPattern = Regex("""Harry[- ]Potter|Hogwarts|Wizarding World|Dumbledore|Voldemort|Hermine|Hermione|Phantastische Tierwesen|Fantastic Beasts|J\.? ?K\.? Rowling""", RegexOption.IGNORE_CASE)

    fun isOnTopic(text: String): Boolean = topicPattern.containsMatchIn(text)

    private val emmaPattern = Regex("""Emma Watson""", RegexOption.IGNORE_CASE)

    fun mentionsEmmaWatson(text: String): Boolean = emmaPattern.containsMatchIn(text)
}
