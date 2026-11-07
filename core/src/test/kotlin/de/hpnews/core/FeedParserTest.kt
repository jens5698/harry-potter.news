package de.hpnews.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedParserTest {
    private val bingRss = """
        <?xml version="1.0" encoding="utf-8"?>
        <rss xmlns:News="https://www.bing.com/news/search?q=x&amp;format=rss" version="2.0">
          <channel>
            <title>Harry Potter - BingNews</title>
            <item>
              <title>Harry-Potter-Serie: HBO zeigt ersten Trailer</title>
              <link>http://www.bing.com/news/apiclick.aspx?ref=FexRss&amp;aid=&amp;tid=1&amp;url=https%3a%2f%2fwww.example.de%2fkultur%2fhp-serie-trailer&amp;c=1&amp;mkt=de-de</link>
              <description>Die neue Harry-Potter-Serie von HBO bekommt einen Trailer. Die Fans sind begeistert.</description>
              <pubDate>Sat, 07 Nov 2026 08:15:00 GMT</pubDate>
              <News:Source>Beispiel Zeitung</News:Source>
            </item>
            <item>
              <title>Völlig anderes Thema ohne Bezug</title>
              <link>http://www.bing.com/news/apiclick.aspx?url=https%3a%2f%2fexample.com%2fx</link>
              <description>Fußball am Wochenende.</description>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    private val wpRss = """
        <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
          <channel>
            <item>
              <title>New Hogwarts Legacy update announced</title>
              <link>https://www.mugglenet.com/2026/11/new-update/</link>
              <pubDate>Fri, 06 Nov 2026 18:00:00 +0000</pubDate>
              <description><![CDATA[<p>Short teaser.</p>]]></description>
              <content:encoded><![CDATA[<p>The first paragraph is about the game.</p><p>The second one too.</p>]]></content:encoded>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    @Test
    fun parsesBingAndUnwrapsLink() {
        val src = Source("Bing DE", "x", Lang.DE, isAggregator = true)
        val items = FeedParser.parse(bingRss, src)
        assertEquals(1, items.size, "off-topic item must be filtered")
        val it = items.single()
        assertEquals("https://www.example.de/kultur/hp-serie-trailer", it.link)
        assertEquals("Beispiel Zeitung", it.publisher)
        assertEquals(Lang.DE, it.language)
        assertTrue(it.isHboSeries)
        assertTrue(it.published != null)
    }

    @Test
    fun parsesWordpressFullContent() {
        val src = Source("MuggleNet", "x", Lang.EN)
        val it = FeedParser.parse(wpRss, src).single()
        assertEquals("MuggleNet", it.publisher)
        val paras = ArticleExtractor().paragraphsFromHtml(it.feedContentHtml!!)
        assertEquals(listOf("The first paragraph is about the game.", "The second one too."), paras)
    }

    @Test
    fun detectsLanguage() {
        assertEquals(Lang.DE, LanguageDetector.detect("Die Serie wird im nächsten Jahr bei HBO und Sky zu sehen sein"))
        assertEquals(Lang.EN, LanguageDetector.detect("The series will be shown on HBO next year and it is the first"))
    }

    @Test
    fun extractsMainText() {
        val html = """
            <html><body><nav><p>Menu item that is long enough to count maybe here</p></nav>
            <article><h1>Title</h1>
              <p>This is the first real paragraph of the article with enough words.</p>
              <p>Werbung</p>
              <p>And this is the second real paragraph, also long enough to be kept.</p>
            </article><footer><p>Copyright footer text that is quite long as well</p></footer></body></html>
        """.trimIndent()
        val paras = ArticleExtractor().extract(org.jsoup.Jsoup.parse(html))
        assertEquals(2, paras.size)
    }

    @Test
    fun dedupesByTitle() {
        val a = NewsItem("1", "HBO: Neue Serie!", "https://a.de/x", "A", "v", null, "", null, Lang.DE, true)
        val b = a.copy(id = "2", link = "https://b.de/y", title = "HBO – neue Serie")
        assertEquals(1, NewsRepository.dedupe(listOf(a, b)).size)
    }
}
