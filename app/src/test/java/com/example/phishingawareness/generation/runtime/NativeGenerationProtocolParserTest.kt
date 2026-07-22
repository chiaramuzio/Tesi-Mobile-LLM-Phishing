package com.example.phishingawareness.generation.runtime

import org.junit.Assert
import org.junit.Test

class NativeGenerationProtocolParserTest {

    private val parser =
        NativeGenerationProtocolParser()

    @Test
    fun parse_validSuccess_returnsStructuredResult() {
        val result =
            parser.parse(
                "OK|GREEDY_SEQUENCE" +
                        "|REQUESTED_TOKEN_COUNT|4" +
                        "|GENERATED_TOKEN_COUNT|2" +
                        "|EOG|0" +
                        "|TOKEN_IDS|10,20" +
                        "|OUTPUT_HEX|4369616f"
            )

        Assert.assertTrue(
            result is NativeGenerationResult.Success
        )

        result as NativeGenerationResult.Success

        Assert.assertEquals(
            4,
            result.requestedTokenCount
        )

        Assert.assertEquals(
            2,
            result.generatedTokenCount
        )

        Assert.assertFalse(
            result.reachedEndOfGeneration
        )

        Assert.assertEquals(
            listOf(10, 20),
            result.tokenIds
        )

        Assert.assertArrayEquals(
            "Ciao".toByteArray(),
            result.outputBytes
        )
    }

    @Test
    fun parse_knownNativeFailure_mapsFailureCode() {
        val result =
            parser.parse(
                "ERROR|MODEL_NOT_LOADED"
            )

        Assert.assertEquals(
            NativeGenerationResult.Failure(
                code =
                    NativeGenerationFailureCode
                        .MODEL_NOT_LOADED,
                rawResponse =
                    "ERROR|MODEL_NOT_LOADED"
            ),
            result
        )
    }

    @Test
    fun parse_unknownNativeFailure_returnsUnknownCode() {
        val result =
            parser.parse(
                "ERROR|UNEXPECTED_NATIVE_FAILURE"
            )

        Assert.assertEquals(
            NativeGenerationFailureCode
                .UNKNOWN_NATIVE_ERROR,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }

    @Test
    fun parse_invalidHex_returnsMalformedResponse() {
        val result =
            parser.parse(
                "OK|GREEDY_SEQUENCE" +
                        "|REQUESTED_TOKEN_COUNT|4" +
                        "|GENERATED_TOKEN_COUNT|1" +
                        "|EOG|0" +
                        "|TOKEN_IDS|10" +
                        "|OUTPUT_HEX|XYZ"
            )

        Assert.assertEquals(
            NativeGenerationFailureCode
                .MALFORMED_NATIVE_RESPONSE,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }

    @Test
    fun parse_inconsistentTokenCount_returnsMalformedResponse() {
        val result =
            parser.parse(
                "OK|GREEDY_SEQUENCE" +
                        "|REQUESTED_TOKEN_COUNT|4" +
                        "|GENERATED_TOKEN_COUNT|2" +
                        "|EOG|0" +
                        "|TOKEN_IDS|10" +
                        "|OUTPUT_HEX|41"
            )

        Assert.assertEquals(
            NativeGenerationFailureCode
                .MALFORMED_NATIVE_RESPONSE,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }
}