package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.modelruntime.CompactRuntimeLifecycle
import com.example.phishingawareness.domain.modelruntime.CompactRuntimePreparationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparedCompactExerciseGeneratorTest {

    @Test
    fun generate_readyRuntime_preparesGeneratesAndReleasesInOrder() {
        val events =
            mutableListOf<String>()

        val lifecycle =
            RecordingRuntimeLifecycle(
                events = events
            )

        val expectedResult =
            CompactExerciseGenerationResult
                .Failure
                .QuizBuilding(
                    details =
                        "risultato controllato"
                )

        val delegate =
            RecordingCompactExerciseGenerator(
                events = events,
                result = expectedResult
            )

        val result =
            PreparedCompactExerciseGenerator(
                runtimeLifecycle = lifecycle,
                delegate = delegate
            ).generate(
                request = validRequest()
            )

        assertSame(
            expectedResult,
            result
        )

        assertEquals(
            listOf(
                "prepare:8192",
                "generate",
                "release"
            ),
            events
        )

        assertEquals(
            1,
            delegate.invocationCount
        )
    }

    @Test
    fun generate_customContext_forwardsContextSize() {
        val lifecycle =
            RecordingRuntimeLifecycle()

        PreparedCompactExerciseGenerator(
            runtimeLifecycle = lifecycle,
            delegate =
                RecordingCompactExerciseGenerator(),
            contextSize = 4096
        ).generate(
            request = validRequest()
        )

        assertEquals(
            4096,
            lifecycle.receivedContextSize
        )
    }

    @Test
    fun generate_preparationFailure_doesNotInvokeDelegate() {
        val lifecycle =
            RecordingRuntimeLifecycle(
                preparationResult =
                    CompactRuntimePreparationResult
                        .Failure(
                            details =
                                "modello non disponibile"
                        )
            )

        val delegate =
            RecordingCompactExerciseGenerator()

        val result =
            PreparedCompactExerciseGenerator(
                runtimeLifecycle = lifecycle,
                delegate = delegate
            ).generate(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .RuntimeLifecycle

        assertEquals(
            "modello non disponibile",
            failure.details
        )

        assertEquals(
            0,
            delegate.invocationCount
        )

        assertFalse(
            lifecycle.releaseCalled
        )
    }

    @Test
    fun generate_prepareException_returnsTypedFailure() {
        val lifecycle =
            ThrowingPrepareRuntimeLifecycle(
                exception =
                    IllegalStateException(
                        "context non disponibile"
                    )
            )

        val delegate =
            RecordingCompactExerciseGenerator()

        val result =
            PreparedCompactExerciseGenerator(
                runtimeLifecycle = lifecycle,
                delegate = delegate
            ).generate(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .RuntimeLifecycle

        assertTrue(
            failure.details.contains(
                "context non disponibile"
            )
        )

        assertEquals(
            0,
            delegate.invocationCount
        )
    }

    @Test
    fun generate_delegateException_releasesAndReturnsTypedFailure() {
        val lifecycle =
            RecordingRuntimeLifecycle()

        val delegate =
            ThrowingCompactExerciseGenerator(
                exception =
                    IllegalStateException(
                        "errore durante la generazione"
                    )
            )

        val result =
            PreparedCompactExerciseGenerator(
                runtimeLifecycle = lifecycle,
                delegate = delegate
            ).generate(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .RuntimeLifecycle

        assertTrue(
            failure.details.contains(
                "errore durante la generazione"
            )
        )

        assertTrue(
            lifecycle.releaseCalled
        )
    }

    @Test
    fun generate_releaseException_preservesDelegateResult() {
        val expectedResult =
            CompactExerciseGenerationResult
                .Failure
                .QuizBuilding(
                    details =
                        "risultato da conservare"
                )

        val lifecycle =
            RecordingRuntimeLifecycle(
                releaseException =
                    IllegalStateException(
                        "errore nel rilascio"
                    )
            )

        val result =
            PreparedCompactExerciseGenerator(
                runtimeLifecycle = lifecycle,
                delegate =
                    RecordingCompactExerciseGenerator(
                        result = expectedResult
                    )
            ).generate(
                request = validRequest()
            )

        assertSame(
            expectedResult,
            result
        )

        assertTrue(
            lifecycle.releaseCalled
        )
    }

    @Test
    fun constructor_invalidContextSize_throws() {
        val exception =
            try {
                PreparedCompactExerciseGenerator(
                    runtimeLifecycle =
                        RecordingRuntimeLifecycle(),
                    delegate =
                        RecordingCompactExerciseGenerator(),
                    contextSize = 0
                )

                null
            } catch (
                caught: IllegalArgumentException
            ) {
                caught
            }

        requireNotNull(exception)

        assertTrue(
            exception.message.orEmpty()
                .contains("contextSize")
        )
    }

    private fun validRequest():
            GenerationRequest {
        return GenerationRequest(
            scenarioId = "ACCOUNT_IT",
            difficulty = "MEDIUM",
            length = "MEDIUM"
        )
    }

    private class RecordingRuntimeLifecycle(
        private val events:
        MutableList<String> =
            mutableListOf(),
        private val preparationResult:
        CompactRuntimePreparationResult =
            CompactRuntimePreparationResult.Ready,
        private val releaseException:
        RuntimeException? = null
    ) : CompactRuntimeLifecycle {

        var receivedContextSize: Int? = null
            private set

        var releaseCalled: Boolean = false
            private set

        override fun prepare(
            contextSize: Int
        ): CompactRuntimePreparationResult {
            receivedContextSize = contextSize
            events += "prepare:$contextSize"

            return preparationResult
        }

        override fun release() {
            releaseCalled = true
            events += "release"

            releaseException?.let { exception ->
                throw exception
            }
        }
    }

    private class ThrowingPrepareRuntimeLifecycle(
        private val exception:
        RuntimeException
    ) : CompactRuntimeLifecycle {

        override fun prepare(
            contextSize: Int
        ): CompactRuntimePreparationResult {
            throw exception
        }

        override fun release() = Unit
    }

    private class RecordingCompactExerciseGenerator(
        private val events:
        MutableList<String> =
            mutableListOf(),
        private val result:
        CompactExerciseGenerationResult =
            CompactExerciseGenerationResult
                .Failure
                .QuizBuilding(
                    details =
                        "risultato di test"
                )
    ) : CompactExerciseGenerator {

        var invocationCount: Int = 0
            private set

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            invocationCount += 1
            events += "generate"

            return result
        }
    }

    private class ThrowingCompactExerciseGenerator(
        private val exception:
        RuntimeException
    ) : CompactExerciseGenerator {

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            throw exception
        }
    }
}