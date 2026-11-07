package de.hpnews.core

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class SimplifierSchemaTest {
    @Test
    fun jsonRoundTripAndCache() {
        val a = SimplifiedArticle("Title", listOf("One.", "Two."), listOf(VocabEntry("to announce", "ankündigen")))
        assertEquals(a, SimplifiedJson.parse(SimplifiedJson.toJson(a)))
        val cache = SimplifiedCache(Files.createTempDirectory("c").toFile())
        cache.put("id", "claude-opus-5-5", a)
        assertEquals(a, cache.get("id", "claude-opus-5-5"))
    }

    @Test
    fun simplifierBuildsWithoutReflection() {
        // Baut die Anfrage (inkl. Schema) – darf keine Klassen-Reflection brauchen.
        Simplifier("dummy-key")
        val schema = Simplifier.OUTPUT_FORMAT.toString()
        println(schema)
        kotlin.test.assertTrue("vocabulary" in schema && "german" in schema)
    }
}
