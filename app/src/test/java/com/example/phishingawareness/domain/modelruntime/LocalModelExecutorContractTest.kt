package com.example.phishingawareness.domain.modelruntime

import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelExecutorContractTest {

    @Test
    fun execute_fakeExecutor_returnsRawOutputAndMetadata() {
        val executor =
            FakeLocalModelExecutor()

        val result =
            executor.execute(
                request()
            )

        assertTrue(
            result is LocalModelExecutionResult.Success
        )

        val success =
            result as LocalModelExecutionResult.Success

        assertEquals(
            """{"scenario":"test"}""",
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

    private fun request(): LocalModelExecutionRequest {
        return LocalModelExecutionRequest(
            prompt = "Prompt di test",
            promptSha256 = "abc123",
            seed = 101,
            contextSize = 8192,
            maxGeneratedTokens = 1200,
            temperature = 0.4f,
            topK = 40,
            topP = 0.90f,
            minP = 0.05f,
            repeatPenalty = 1.05f
        )
    }

    private class FakeLocalModelExecutor :
        LocalModelExecutor {

        override fun execute(
            request: LocalModelExecutionRequest
        ): LocalModelExecutionResult {
            val output =
                """{"scenario":"test"}"""

            return LocalModelExecutionResult.Success(
                rawOutput = output,
                metadata =
                    LocalModelExecutionMetadata(
                        promptSha256 =
                            request.promptSha256,
                        seed = request.seed,
                        generatedCharacterCount =
                            output.length
                    )
            )
        }
    }
}