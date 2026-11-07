package de.hpnews.core

import com.anthropic.models.messages.MessageCreateParams
import kotlin.test.Test
import kotlin.test.assertTrue

class SimplifierSchemaTest {
    @Test
    fun schemaIsDerivedFromClass() {
        val params = MessageCreateParams.builder()
            .model(Simplifier.DEFAULT_MODEL)
            .maxTokens(100L)
            .outputConfig(SimplifiedSchema::class.java)
            .addUserMessage("x")
            .build()
        val json = params.rawParams.outputConfig().get().toString()
        println(json)
        assertTrue(json.contains("vocabulary") && json.contains("german"))
    }
}
