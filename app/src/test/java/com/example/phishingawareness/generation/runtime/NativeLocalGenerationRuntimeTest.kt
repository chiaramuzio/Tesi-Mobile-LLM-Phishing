package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeLocalGenerationRuntimeTest {

    @Test
    fun generate_forwardsPromptAndParametersToGateway() {
        val expectedParameters =
            LocalGenerationParameters(
                maxGeneratedTokens = 1200,
                temperature = 0.4f,
                topK = 40,
                topP = 0.90f,
                minP = 0.05f,
                repeatPenalty = 1.05f,
                seed = 101
            )

        val expectedResult =
            NativeGeneratedSequence(
                requestedTokenCount = 1200,
                generatedTokenCount = 3,
                reachedEndOfGeneration = true,
                tokenIds = listOf(10, 20, 30),
                rawText = """{"sender":"Test"}"""
            )

        var receivedPrompt: String? = null
        var receivedAddSpecial: Boolean? = null
        var receivedParameters:
                LocalGenerationParameters? = null

        val gateway =
            NativeGenerationGateway {
                    prompt,
                    addSpecial,
                    parameters ->

                receivedPrompt = prompt
                receivedAddSpecial = addSpecial
                receivedParameters = parameters

                expectedResult
            }

        val runtime =
            NativeLocalGenerationRuntime(
                gateway = gateway,
                addSpecialTokens = true
            )

        val actualResult =
            runtime.generate(
                prompt = "PROMPT COMPLETO",
                parameters = expectedParameters
            )

        assertEquals(
            "PROMPT COMPLETO",
            receivedPrompt
        )

        assertEquals(
            true,
            receivedAddSpecial
        )

        assertEquals(
            expectedParameters,
            receivedParameters
        )

        assertEquals(
            expectedResult,
            actualResult
        )
    }

    @Test
    fun generate_whenSpecialTokensAreDisabled_forwardsFalse() {
        var receivedAddSpecial: Boolean? = null

        val gateway =
            NativeGenerationGateway {
                    _,
                    addSpecial,
                    parameters ->

                receivedAddSpecial = addSpecial

                NativeGeneratedSequence(
                    requestedTokenCount =
                        parameters.maxGeneratedTokens,
                    generatedTokenCount = 0,
                    reachedEndOfGeneration = true,
                    tokenIds = emptyList(),
                    rawText = ""
                )
            }

        val runtime =
            NativeLocalGenerationRuntime(
                gateway = gateway,
                addSpecialTokens = false
            )

        runtime.generate(
            prompt = "PROMPT",
            parameters = validParameters()
        )

        assertEquals(
            false,
            receivedAddSpecial
        )
    }

    @Test
    fun generate_blankPrompt_throwsBeforeCallingGateway() {
        var gatewayCalled = false

        val gateway =
            NativeGenerationGateway {
                    _,
                    _,
                    parameters ->

                gatewayCalled = true

                NativeGeneratedSequence(
                    requestedTokenCount =
                        parameters.maxGeneratedTokens,
                    generatedTokenCount = 0,
                    reachedEndOfGeneration = false,
                    tokenIds = emptyList(),
                    rawText = ""
                )
            }

        val runtime =
            NativeLocalGenerationRuntime(
                gateway = gateway
            )

        assertThrows(
            IllegalArgumentException::class.java
        ) {
            runtime.generate(
                prompt = "   ",
                parameters = validParameters()
            )
        }

        assertTrue(
            !gatewayCalled
        )
    }

    @Test
    fun parameters_invalidTopP_throws() {
        assertThrows(
            IllegalArgumentException::class.java
        ) {
            LocalGenerationParameters(
                maxGeneratedTokens = 1200,
                temperature = 0.4f,
                topK = 40,
                topP = 1.1f,
                minP = 0.05f,
                repeatPenalty = 1.05f,
                seed = 101
            )
        }
    }

    private fun validParameters():
            LocalGenerationParameters {
        return LocalGenerationParameters(
            maxGeneratedTokens = 1200,
            temperature = 0.4f,
            topK = 40,
            topP = 0.90f,
            minP = 0.05f,
            repeatPenalty = 1.05f,
            seed = 101
        )
    }
}