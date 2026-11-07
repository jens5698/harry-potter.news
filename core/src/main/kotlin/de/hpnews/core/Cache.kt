package de.hpnews.core

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/** Speichert Übersetzungen, damit jeder Artikel nur einmal an die KI geht (spart Kosten). */
class SimplifiedCache(private val dir: File) {
    private val mapper = jacksonObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

    init { dir.mkdirs() }

    private fun file(id: String, model: String) = File(dir, "$id-${model.replace(Regex("[^a-z0-9-]"), "")}.json")

    fun get(id: String, model: String): SimplifiedArticle? {
        val f = file(id, model)
        if (!f.exists()) return null
        return runCatching { mapper.readValue<SimplifiedArticle>(f) }.getOrNull()
    }

    fun put(id: String, model: String, article: SimplifiedArticle) {
        mapper.writeValue(file(id, model), article)
    }
}
