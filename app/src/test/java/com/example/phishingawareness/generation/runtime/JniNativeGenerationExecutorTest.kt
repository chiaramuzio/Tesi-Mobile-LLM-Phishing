package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JniNativeGenerationExecutorTest {

    @Test
    fun generate_blankPrompt_returnsFailureWithoutNativeCall() {
        var nativeCallCount = 0

        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator { _, _, _ ->
                        nativeCallCount += 1
                        error("La chiamata nativa non era attesa")
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest(
                        prompt = " "
                    )
            )

        assertEquals(
            0,
            nativeCallCount
        )

        assertEquals(
            NativeGenerationFailureCode.PROMPT_EMPTY,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }

    @Test
    fun generate_invalidSampling_returnsFailureWithoutNativeCall() {
        var nativeCallCount = 0

        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator { _, _, _ ->
                        nativeCallCount += 1
                        error("La chiamata nativa non era attesa")
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest(
                        sampling =
                            NativeSamplingConfiguration
                                .ThesisDefault
                                .copy(
                                    temperature = 0.0f
                                )
                    )
            )

        assertEquals(
            0,
            nativeCallCount
        )

        result as NativeGenerationResult.Failure

        assertEquals(
            NativeGenerationFailureCode
                .INVALID_SAMPLING_CONFIGURATION,
            result.code
        )

        assertTrue(
            result.rawResponse.contains(
                "INVALID_TEMPERATURE"
            )
        )
    }

    @Test
    fun generate_probeLimitExceeded_returnsControlledFailure() {
        var nativeCallCount = 0

        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator { _, _, _ ->
                        nativeCallCount += 1
                        error("La chiamata nativa non era attesa")
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest(
                        sampling =
                            NativeSamplingConfiguration
                                .ThesisDefault
                                .copy(
                                    maxGeneratedTokens = 9
                                )
                    )
            )

        assertEquals(
            0,
            nativeCallCount
        )

        assertEquals(
            NativeGenerationFailureCode
                .INVALID_MAX_GENERATED_TOKENS,
            (result as NativeGenerationResult.Failure)
                .code
        )

        assertEquals(
            "KOTLIN|GREEDY_PROBE_LIMIT_EXCEEDED",
            result.rawResponse
        )
    }

    @Test
    fun generate_validRequest_forwardsExpectedArguments() {
        var receivedPrompt: String? = null
        var receivedAddSpecial: Boolean? = null
        var receivedMaxGeneratedTokens: Int? = null

        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator {
                            prompt,
                            addSpecial,
                            maxGeneratedTokens ->

                        receivedPrompt = prompt
                        receivedAddSpecial = addSpecial
                        receivedMaxGeneratedTokens =
                            maxGeneratedTokens

                        "OK|GREEDY_SEQUENCE" +
                                "|REQUESTED_TOKEN_COUNT|4" +
                                "|GENERATED_TOKEN_COUNT|2" +
                                "|EOG|0" +
                                "|TOKEN_IDS|10,20" +
                                "|OUTPUT_HEX|4369616f"
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest()
            )

        assertEquals(
            "Prompt di prova",
            receivedPrompt
        )

        assertEquals(
            true,
            receivedAddSpecial
        )

        assertEquals(
            4,
            receivedMaxGeneratedTokens
        )

        result as NativeGenerationResult.Success

        assertEquals(
            4,
            result.requestedTokenCount
        )

        assertEquals(
            2,
            result.generatedTokenCount
        )

        assertFalse(
            result.reachedEndOfGeneration
        )

        assertEquals(
            listOf(10, 20),
            result.tokenIds
        )

        assertArrayEquals(
            "Ciao".toByteArray(),
            result.outputBytes
        )
    }

    @Test
    fun generate_nativeFailure_returnsParsedFailure() {
        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator { _, _, _ ->
                        "ERROR|CONTEXT_NOT_CREATED"
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest()
            )

        assertEquals(
            NativeGenerationFailureCode
                .CONTEXT_NOT_CREATED,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }

    @Test
    fun generate_malformedNativeResponse_returnsParsedFailure() {
        val executor =
            JniNativeGenerationExecutor(
                nativeSequenceGenerator =
                    NativeSequenceGenerator { _, _, _ ->
                        "RISPOSTA_NON_VALIDA"
                    }
            )

        val result =
            executor.generate(
                request =
                    validRequest()
            )

        assertEquals(
            NativeGenerationFailureCode
                .MALFORMED_NATIVE_RESPONSE,
            (result as NativeGenerationResult.Failure)
                .code
        )
    }

    private fun validRequest(
        prompt: String = "Prompt di prova",
        sampling: NativeSamplingConfiguration =
            NativeSamplingConfiguration
                .ThesisDefault
                .copy(
                    maxGeneratedTokens = 4
                )
    ): NativeGenerationRequest =
        NativeGenerationRequest(
            prompt = prompt,
            addSpecial = true,
            sampling = sampling
        )
}