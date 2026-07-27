package com.example.phishingawareness.generation.prompt

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gemma1BCompactOutputContractTest {

    @Test
    fun content_containsEveryFieldRequiredByCurrentParser() {
        val content =
            Gemma1BCompactOutputContract.content

        REQUIRED_FIELDS.forEach { field ->
            assertTrue(
                "Campo obbligatorio assente: $field",
                content.contains(field)
            )
        }
    }

    @Test
    fun content_doesNotContainCopyableStringPlaceholders() {
        val content =
            Gemma1BCompactOutputContract.content

        assertFalse(
            content.contains("\"string\"")
        )
    }

    @Test
    fun content_requiresRawJsonWithoutMarkdown() {
        val content =
            Gemma1BCompactOutputContract.content

        assertTrue(
            content.contains(
                "Non usare Markdown o blocchi di codice."
            )
        )

        assertTrue(
            content.contains(
                "Il primo carattere deve essere {"
            )
        )

        assertTrue(
            content.contains(
                "L'ultimo carattere deve essere }"
            )
        )
    }

    private companion object {

        val REQUIRED_FIELDS =
            listOf(
                "scenario",
                "difficulty",
                "length",
                "sender_name",
                "sender_address",
                "recipient",
                "subject",
                "body",
                "pretext",
                "cta_type",
                "cta_text",
                "present_indicators",
                "credibility_elements",
                "educational_summary",
                "id",
                "evidence",
                "explanation"
            )
    }
}