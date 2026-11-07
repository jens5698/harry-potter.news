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

    private val hboPattern = Regex("""\bHBO\b|\bMax\b.*(serie|series)|(serie|series).*\bMax\b|TV[- ]?(serie|series)|Fernsehserie|Paapa Essiedu|Dominic McLaughlin|John Lithgow|Arabella Stanton|Alastair Stout""", RegexOption.IGNORE_CASE)

    fun looksLikeHboSeries(text: String): Boolean = hboPattern.containsMatchIn(text)

    private val topicPattern = Regex("""Harry[- ]Potter|Hogwarts|Wizarding World|Dumbledore|Voldemort|Hermine|Hermione|Phantastische Tierwesen|Fantastic Beasts|J\.? ?K\.? Rowling""", RegexOption.IGNORE_CASE)

    fun isOnTopic(text: String): Boolean = topicPattern.containsMatchIn(text)
}
