package com.example.phishingawareness.generation.output

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeterministicJsonObjectExtractorTest {

    private val extractor =
        DeterministicJsonObjectExtractor()

    @Test
    fun extract_jsonOnly_returnsUnchangedJson() {
        val rawOutput =
            """{"scenario":"Bancario e pagamenti"}"""

        assertEquals(
            rawOutput,
            extractor.extract(rawOutput)
        )
    }

    @Test
    fun extract_emptyThinkWrapperBeforeJson_returnsJsonOnly() {
        val rawOutput =
            """
            <think>

            </think>
            {"scenario":"Bancario e pagamenti"}
            """.trimIndent()

        assertEquals(
            """{"scenario":"Bancario e pagamenti"}""",
            extractor.extract(rawOutput)
        )
    }

    @Test
    fun extract_textAfterJson_returnsJsonOnly() {
        val rawOutput =
            """
            {"scenario":"Account e sicurezza IT"}
            testo esterno
            """.trimIndent()

        assertEquals(
            """{"scenario":"Account e sicurezza IT"}""",
            extractor.extract(rawOutput)
        )
    }

    @Test
    fun extract_multilineJson_preservesInternalContent() {
        val rawOutput =
            """
            prefisso
            {
              "scenario": "Bancario e pagamenti",
              "body": "Testo"
            }
            suffisso
            """.trimIndent()

        val expected =
            """
            {
              "scenario": "Bancario e pagamenti",
              "body": "Testo"
            }
            """.trimIndent()

        assertEquals(
            expected,
            extractor.extract(rawOutput)
        )
    }

    @Test
    fun extract_blankOutput_returnsNull() {
        assertNull(
            extractor.extract("   ")
        )
    }

    @Test
    fun extract_outputWithoutJsonObject_returnsNull() {
        assertNull(
            extractor.extract(
                "<think></think>"
            )
        )
    }

    @Test
    fun extract_unclosedJsonObject_returnsNull() {
        assertNull(
            extractor.extract(
                """prefisso {"scenario":"BANKING""""
            )
        )
    }
}