package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NativeSamplingValidationProtocolParserTest {

    private val parser =
        NativeSamplingValidationProtocolParser()

    @Test
    fun parse_validResponse_returnsConfiguration() {
        val configuration =
            NativeSamplingConfiguration.ThesisDefault

        val result =
            parser.parse(
                rawResponse =
                    "OK|SAMPLING_CONFIGURATION" +
                            "|MAX_GENERATED_TOKENS|1200" +
                            "|TEMPERATURE|0.4" +
                            "|TOP_K|40" +
                            "|TOP_P|0.9" +
                            "|MIN_P|0.05" +
                            "|REPEAT_PENALTY|1.05" +
                            "|SEED|101",
                configuration = configuration
            )

        result as NativeSamplingValidationResult.Valid

        assertSame(
            configuration,
            result.configuration
        )
    }

    @Test
    fun parse_nativeErrors_returnExpectedCodes() {
        val expectedResults =
            mapOf(
                "ERROR|INVALID_MAX_GENERATED_TOKENS" to
                        NativeSamplingValidationFailureCode
                            .INVALID_MAX_GENERATED_TOKENS,

                "ERROR|INVALID_TEMPERATURE" to
                        NativeSamplingValidationFailureCode
                            .INVALID_TEMPERATURE,

                "ERROR|INVALID_TOP_K" to
                        NativeSamplingValidationFailureCode
                            .INVALID_TOP_K,

                "ERROR|INVALID_TOP_P" to
                        NativeSamplingValidationFailureCode
                            .INVALID_TOP_P,

                "ERROR|INVALID_MIN_P" to
                        NativeSamplingValidationFailureCode
                            .INVALID_MIN_P,

                "ERROR|INVALID_REPEAT_PENALTY" to
                        NativeSamplingValidationFailureCode
                            .INVALID_REPEAT_PENALTY
            )

        expectedResults.forEach {
                rawResponse,
                expectedCode ->

            val result =
                parser.parse(
                    rawResponse = rawResponse,
                    configuration =
                        NativeSamplingConfiguration
                            .ThesisDefault
                )

            assertEquals(
                expectedCode,
                (
                        result as
                                NativeSamplingValidationResult.Invalid
                        ).code
            )
        }
    }

    @Test
    fun parse_unknownResponse_returnsMalformedFailure() {
        val result =
            parser.parse(
                rawResponse =
                    "RISPOSTA_NON_VALIDA",
                configuration =
                    NativeSamplingConfiguration
                        .ThesisDefault
            )

        assertEquals(
            NativeSamplingValidationFailureCode
                .MALFORMED_NATIVE_RESPONSE,
            (
                    result as
                            NativeSamplingValidationResult.Invalid
                    ).code
        )
    }
}