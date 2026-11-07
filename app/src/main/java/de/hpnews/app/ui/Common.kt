package de.hpnews.app.ui

import de.hpnews.core.Lang
import de.hpnews.core.NewsItem
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("d. MMM yyyy, HH:mm", Locale.GERMAN).withZone(ZoneId.systemDefault())

fun formatDate(i: Instant?): String {
    if (i == null) return ""
    val ago = Duration.between(i, Instant.now())
    return when {
        ago.isNegative -> dateFmt.format(i)
        ago.toMinutes() < 60 -> "vor ${ago.toMinutes()} Min."
        ago.toHours() < 24 -> "vor ${ago.toHours()} Std."
        ago.toDays() < 7 -> "vor ${ago.toDays()} Tag" + if (ago.toDays() == 1L) "" else "en"
        else -> dateFmt.format(i)
    }
}

fun langLabel(item: NewsItem) = when (item.language) {
    Lang.DE -> "DE"
    Lang.EN -> "EN"
    Lang.OTHER -> "??"
}
