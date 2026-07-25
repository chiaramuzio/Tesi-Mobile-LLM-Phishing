package com.example.phishingawareness.generation.runtime

import com.example.phishingawareness.domain.model.LocalModelExecutionFailureCode
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeLocalModelExecutorTest {

    @Test
    fun execute_validRequest_forwardsPromptAndParameters() {
        var receivedPrompt: String? = null
        var receivedParameters:
                LocalGenerationParameters? = null

        val runtime =
            LocalGenerationRuntime {
                    prompt,
                    parameters ->

                receivedPrompt = prompt
                receivedParameters = parameters

                NativeGeneratedSequence(
                    requestedTokenCount =
                        parameters.maxGeneratedTokens,
                    generatedTokenCount = 3,
                    reachedEndOfGeneration = true,
                    tokenIds =
                        listOf(10, 20, 30),
                    rawText =
                        """{"scenario":"BANKING"}"""
                )
            }

        val executor =
            RuntimeLocalModelExecutor(
                runtime = runtime
            )

        val result =
            executor.execute(
                request = validRequest()
            )

        assertEquals(
            "PROMPT COMPLETO",
            receivedPrompt
        )

        assertEquals(
            LocalGenerationParameters(
                maxGeneratedTokens = 1200,
                temperature = 0.4f,
                topK = 40,
                topP = 0.90f,
                minP = 0.05f,
                repeatPenalty = 1.05f,
                seed = 101
            ),
            receivedParameters
        )

        assertTrue(
            result is LocalModelExecutionResult.Success
        )

        val success =
            result as LocalModelExecutionResult.Success

        assertEquals(
            """{"scenario":"BANKING"}""",
            success.rawOutput
        )

        assertEquals(
            "abc123",
            success.metadata.promptSha256
        )

        assertEquals(
            101,
            success.metadata.seed
        )

        assertEquals(
            success.rawOutput.length,
            success.metadata.generatedCharacterCount
        )
    }

    @Test
    fun execute_blankPrompt_returnsInvalidRequestWithoutRuntimeCall() {
        var runtimeCalled = false

        val runtime =
            LocalGenerationRuntime { _, parameters ->
                runtimeCalled = true

                emptySequence(
                    parameters = parameters
                )
            }

        val executor =
            RuntimeLocalModelExecutor(
                runtime = runtime
            )

        val result =
            executor.execute(
                request =
                    validRequest().copy(
                        prompt = " "
                    )
            )

        assertFalse(runtimeCalled)

        assertEquals(
            LocalModelExecutionFailureCode
                .INVALID_REQUEST,
            (result as LocalModelExecutionResult.Failure)
                .code
        )
    }

    @Test
    fun execute_invalidSampling_returnsInvalidRequestWithoutRuntimeCall() {
        var runtimeCalled = false

        val runtime =
            LocalGenerationRuntime { _, parameters ->
                runtimeCalled = true

                emptySequence(
                    parameters = parameters
                )
            }

        val executor =
            RuntimeLocalModelExecutor(
                runtime = runtime
            )

        val result =
            executor.execute(
                request =
                    validRequest().copy(
                        topP = 1.1f
                    )
            )

        assertFalse(runtimeCalled)

        assertEquals(
            LocalModelExecutionFailureCode
                .INVALID_REQUEST,
            (result as LocalModelExecutionResult.Failure)
                .code
        )
    }

    @Test
    fun execute_emptyRuntimeOutput_returnsEmptyOutput() {
        val runtime =
            LocalGenerationRuntime { _, parameters ->
                emptySequence(
                    parameters = parameters
                )
            }

        val executor =
            RuntimeLocalModelExecutor(
                runtime = runtime
            )

        val result =
            executor.execute(
                request = validRequest()
            )

        assertEquals(
            LocalModelExecutionFailureCode
                .EMPTY_OUTPUT,
            (result as LocalModelExecutionResult.Failure)
                .code
        )
    }

    @Test
    fun execute_runtimeException_returnsExecutionFailed() {
        val runtime =
            LocalGenerationRuntime { _, _ ->
                throw IllegalStateException(
                    "Context non disponibile"
                )
            }

        val executor =
            RuntimeLocalModelExecutor(
                runtime = runtime
            )

        val result =
            executor.execute(
                request = validRequest()
            )

        assertEquals(
            LocalModelExecutionFailureCode
                .EXECUTION_FAILED,
            (result as LocalModelExecutionResult.Failure)
                .code
        )

        assertEquals(
            "Context non disponibile",
            result.details
        )
    }

    private fun validRequest():
            LocalModelExecutionRequest {
        return LocalModelExecutionRequest(
            prompt =
                "PROMPT COMPLETO",
            promptSha256 =
                "abc123",
            seed =
                101,
            contextSize =
                8192,
            maxGeneratedTokens =
                1200,
            temperature =
                0.4f,
            topK =
                40,
            topP =
                0.90f,
            minP =
                0.05f,
            repeatPenalty =
                1.05f
        )
    }

    private fun emptySequence(
        parameters: LocalGenerationParameters
    ): NativeGeneratedSequence {
        return NativeGeneratedSequence(
            requestedTokenCount =
                parameters.maxGeneratedTokens,
            generatedTokenCount = 0,
            reachedEndOfGeneration = true,
            tokenIds = emptyList(),
            rawText = ""
        )
    }
}