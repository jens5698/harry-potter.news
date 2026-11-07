package de.hpnews.core

import java.io.File

/** Speichert Übersetzungen, damit jeder Artikel nur einmal an die KI geht (spart Kosten). */
class SimplifiedCache(private val dir: File) {

    init { dir.mkdirs() }

    private fun file(id: String, model: String) = File(dir, "$id-${model.replace(Regex("[^a-z0-9-]"), "")}.json")

    fun get(id: String, model: String): SimplifiedArticle? {
        val f = file(id, model)
        if (!f.exists()) return null
        return runCatching { SimplifiedJson.parse(f.readText()) }.getOrNull()
    }

    fun put(id: String, model: String, article: SimplifiedArticle) {
        file(id, model).writeText(SimplifiedJson.toJson(article))
    }
}
