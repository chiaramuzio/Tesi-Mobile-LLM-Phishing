package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class JniNativeSamplingConfigurationValidatorTest {

    @Test
    fun validate_invalidKotlinConfiguration_skipsNativeCall() {
        var nativeCalls = 0

        val validator =
            JniNativeSamplingConfigurationValidator(
                rawValidator =
                    NativeSamplingRawValidator {
                        nativeCalls += 1
                        error(
                            "La chiamata nativa non era attesa"
                        )
                    }
            )

        val result =
            validator.validate(
                NativeSamplingConfiguration
                    .ThesisDefault
                    .copy(
                        topK = 0
                    )
            )

        assertEquals(
            0,
            nativeCalls
        )

        assertEquals(
            NativeSamplingValidationFailureCode
                .INVALID_TOP_K,
            (
                    result as
                            NativeSamplingValidationResult.Invalid
                    ).code
        )
    }

    @Test
    fun validate_validConfiguration_forwardsAllValues() {
        var receivedConfiguration:
                NativeSamplingConfiguration? = null

        val expectedConfiguration =
            NativeSamplingConfiguration
                .ThesisDefault

        val validator =
            JniNativeSamplingConfigurationValidator(
                rawValidator =
                    NativeSamplingRawValidator {
                            configuration ->

                        receivedConfiguration =
                            configuration

                        "OK|SAMPLING_CONFIGURATION" +
                                "|MAX_GENERATED_TOKENS|1200" +
                                "|TEMPERATURE|0.4" +
                                "|TOP_K|40" +
                                "|TOP_P|0.9" +
                                "|MIN_P|0.05" +
                                "|REPEAT_PENALTY|1.05" +
                                "|SEED|101"
                    }
            )

        val result =
            validator.validate(
                expectedConfiguration
            )

        assertSame(
            expectedConfiguration,
            receivedConfiguration
        )

        assertSame(
            expectedConfiguration,
            (
                    result as
                            NativeSamplingValidationResult.Valid
                    ).configuration
        )
    }

    @Test
    fun validate_nativeFailure_returnsParsedFailure() {
        val validator =
            JniNativeSamplingConfigurationValidator(
                rawValidator =
                    NativeSamplingRawValidator {
                        "ERROR|INVALID_TOP_P"
                    }
            )

        val result =
            validator.validate(
                NativeSamplingConfiguration
                    .ThesisDefault
            )

        assertEquals(
            NativeSamplingValidationFailureCode
                .INVALID_TOP_P,
            (
                    result as
                            NativeSamplingValidationResult.Invalid
                    ).code
        )
    }

    @Test
    fun validate_malformedResponse_returnsControlledFailure() {
        val validator =
            JniNativeSamplingConfigurationValidator(
                rawValidator =
                    NativeSamplingRawValidator {
                        "RISPOSTA_SCONOSCIUTA"
                    }
            )

        val result =
            validator.validate(
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