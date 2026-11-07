package de.hpnews.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

/** JSON ohne Reflection lesen/schreiben (funktioniert so sicher auch auf Android). */
object SimplifiedJson {
    private val mapper = ObjectMapper()

    fun parse(json: String): SimplifiedArticle = fromNode(mapper.readTree(json))

    fun fromNode(n: JsonNode): SimplifiedArticle {
        val title = n.path("title").asText("")
        val paragraphs = n.path("paragraphs").map { it.asText("") }.filter { it.isNotBlank() }
        val vocab = n.path("vocabulary").mapNotNull { v ->
            val en = v.path("english").asText("")
            val de = v.path("german").asText("")
            if (en.isBlank() || de.isBlank()) null else VocabEntry(en, de)
        }
        require(paragraphs.isNotEmpty()) { "keine Absätze" }
        return SimplifiedArticle(title, paragraphs, vocab)
    }

    fun toJson(a: SimplifiedArticle): String {
        val root: ObjectNode = mapper.createObjectNode()
        root.put("title", a.title)
        val ps = root.putArray("paragraphs")
        a.paragraphs.forEach { ps.add(it) }
        val vs = root.putArray("vocabulary")
        a.vocabulary.forEach { v -> vs.addObject().put("english", v.english).put("german", v.german) }
        return mapper.writeValueAsString(root)
    }
}
