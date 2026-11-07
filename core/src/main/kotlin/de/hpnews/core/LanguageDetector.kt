package de.hpnews.core

/** Sehr einfache Spracherkennung über häufige Füllwörter. Reicht für DE vs. EN. */
object LanguageDetector {
    private val german = setOf(
        "der", "die", "das", "und", "ist", "nicht", "ein", "eine", "mit", "auf", "für", "von",
        "den", "dem", "sich", "auch", "wird", "werden", "wurde", "bei", "nach", "über", "noch", "wie", "aus",
    )
    private val english = setOf(
        "the", "and", "is", "of", "to", "in", "that", "for", "with", "on", "was", "are", "will",
        "be", "has", "have", "from", "this", "by", "at", "it", "as", "an", "who", "new",
    )

    fun detect(text: String): Lang {
        val words = text.lowercase().split(Regex("[^\\p{L}]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return Lang.OTHER
        val de = words.count { it in german }
        val en = words.count { it in english }
        if (de == 0 && en == 0) return Lang.OTHER
        return if (de > en) Lang.DE else Lang.EN
    }
}
