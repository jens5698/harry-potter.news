package de.hpnews.core

import java.net.URLEncoder

object Sources {
    private fun bing(query: String, market: String): String {
        val q = URLEncoder.encode(query, "UTF-8")
        val lang = market.substringBefore('-')
        val cc = market.substringAfter('-')
        return "https://www.bing.com/news/search?q=$q&format=rss&setlang=$lang&cc=$cc&qft=sortbydate%3d%221%22"
    }

    /** Standardquellen. Lässt sich später leicht erweitern. */
    val default: List<Source> = listOf(
        // Deutschsprachig (Suche über Bing News; Herausgeber steht im Eintrag)
        Source("Bing News (DE): Harry Potter", bing("\"Harry Potter\"", "de-DE"), Lang.DE, isAggregator = true),
        Source("Bing News (DE): HBO-Serie", bing("\"Harry Potter\" Serie HBO", "de-DE"), Lang.DE, isAggregator = true),
        // Englischsprachig
        Source("Bing News (EN): Harry Potter", bing("\"Harry Potter\"", "en-US"), Lang.EN, isAggregator = true),
        Source("Bing News (EN): HBO series", bing("\"Harry Potter\" HBO series", "en-US"), Lang.EN, isAggregator = true),
        Source("MuggleNet", "https://www.mugglenet.com/feed/", Lang.EN),
        Source("The Leaky Cauldron", "https://www.the-leaky-cauldron.org/feed/", Lang.EN),
    )

    /** Kandidaten, die nur von der Diagnose geprüft werden. */
    val candidates: List<Source> = listOf(
        Source("Google News (DE)", "https://news.google.com/rss/search?q=%22Harry+Potter%22&hl=de&gl=DE&ceid=DE:de", Lang.DE, isAggregator = true),
        Source("Bing (DE, einfach)", "https://www.bing.com/news/search?q=Harry+Potter&format=rss&mkt=de-DE", Lang.DE, isAggregator = true),
        Source("Filmstarts", "https://www.filmstarts.de/rss/nachrichten.xml", Lang.DE, isAggregator = false),
        Source("Moviepilot", "https://www.moviepilot.de/rss/news", Lang.DE),
        Source("Serienjunkies", "https://www.serienjunkies.de/rss/news.xml", Lang.DE),
        Source("Quotenmeter", "https://www.quotenmeter.de/rss/quotenmeter.xml", Lang.DE),
        Source("Harry-Potter-Xperts", "https://www.harrypotter-xperts.de/feed", Lang.DE),
        Source("HP-FC", "https://www.hp-fc.de/feed/", Lang.DE),
        Source("Wizarding World", "https://www.wizardingworld.com/rss", Lang.EN),
        Source("Leaky (ohne www)", "https://the-leaky-cauldron.org/feed/", Lang.EN),
        Source("HypeableHP", "https://www.hypable.com/harry-potter/feed/", Lang.EN),
    )

    private val hboPattern = Regex("""\bHBO\b|\bMax\b.*(serie|series)|(serie|series).*\bMax\b|TV[- ]?(serie|series)|Fernsehserie|Paapa Essiedu|Dominic McLaughlin|John Lithgow|Arabella Stanton|Alastair Stout""", RegexOption.IGNORE_CASE)

    fun looksLikeHboSeries(text: String): Boolean = hboPattern.containsMatchIn(text)

    private val topicPattern = Regex("""Harry[- ]Potter|Hogwarts|Wizarding World|Dumbledore|Voldemort|Hermine|Hermione|Phantastische Tierwesen|Fantastic Beasts|J\.? ?K\.? Rowling""", RegexOption.IGNORE_CASE)

    fun isOnTopic(text: String): Boolean = topicPattern.containsMatchIn(text)
}
