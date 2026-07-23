package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeGeneratedSequenceParserTest {

    @Test
    fun parse_singleGeneratedToken_returnsTypedResult() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|1" +
                    "|GENERATED_TOKEN_COUNT|1" +
                    "|EOG|0" +
                    "|TOKEN_IDS|107" +
                    "|OUTPUT_HEX|0a"

        val result =
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )

        assertEquals(
            1,
            result.requestedTokenCount
        )

        assertEquals(
            1,
            result.generatedTokenCount
        )

        assertFalse(
            result.reachedEndOfGeneration
        )

        assertEquals(
            listOf(107),
            result.tokenIds
        )

        assertEquals(
            "\n",
            result.rawText
        )
    }

    @Test
    fun parse_multipleGeneratedTokens_decodesCompleteUtf8Text() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|8" +
                    "|GENERATED_TOKEN_COUNT|8" +
                    "|EOG|0" +
                    "|TOKEN_IDS|107,150917,236888,107,90837,6896,236888,107" +
                    "|OUTPUT_HEX|0a4369616f210a4f7474696d6f210a"

        val result =
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )

        assertEquals(
            8,
            result.requestedTokenCount
        )

        assertEquals(
            8,
            result.generatedTokenCount
        )

        assertEquals(
            "\nCiao!\nOttimo!\n",
            result.rawText
        )
    }

    @Test
    fun parse_endOfGeneration_mapsEogFlag() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|8" +
                    "|GENERATED_TOKEN_COUNT|1" +
                    "|EOG|1" +
                    "|TOKEN_IDS|1" +
                    "|OUTPUT_HEX|"

        val result =
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )

        assertTrue(
            result.reachedEndOfGeneration
        )

        assertEquals(
            "",
            result.rawText
        )
    }

    @Test
    fun parse_generatedTokenCountDifferentFromTokenIds_throws() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|2" +
                    "|GENERATED_TOKEN_COUNT|2" +
                    "|EOG|0" +
                    "|TOKEN_IDS|107" +
                    "|OUTPUT_HEX|0a"

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )
        }
    }

    @Test
    fun parse_oddLengthOutputHex_throws() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|1" +
                    "|GENERATED_TOKEN_COUNT|1" +
                    "|EOG|0" +
                    "|TOKEN_IDS|107" +
                    "|OUTPUT_HEX|a"

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )
        }
    }

    @Test
    fun parse_nonHexadecimalOutput_throws() {
        val protocol =
            "OK|GREEDY_SEQUENCE" +
                    "|REQUESTED_TOKEN_COUNT|1" +
                    "|GENERATED_TOKEN_COUNT|1" +
                    "|EOG|0" +
                    "|TOKEN_IDS|107" +
                    "|OUTPUT_HEX|zz"

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )
        }
    }

    @Test
    fun parse_nativeErrorResult_throws() {
        val protocol =
            "ERROR|MODEL_NOT_LOADED"

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            NativeGeneratedSequenceParser.parse(
                protocol = protocol
            )
        }
    }
}