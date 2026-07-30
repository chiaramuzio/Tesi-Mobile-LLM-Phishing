package com.example.phishingawareness.generation.model

import com.example.phishingawareness.domain.modelruntime.CompactRuntimePreparationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCompactRuntimeLifecycleTest {

    @Test
    fun prepare_forwardsContextAndReturnsResult() {
        var receivedContextSize: Int? = null

        val expectedResult =
            CompactRuntimePreparationResult.Ready

        val lifecycle =
            AndroidCompactRuntimeLifecycle(
                prepareAction = { contextSize ->
                    receivedContextSize =
                        contextSize

                    expectedResult
                },
                releaseAction = {},
                testOnlyMarker = Unit
            )

        val result =
            lifecycle.prepare(
                contextSize = 8192
            )

        assertEquals(
            8192,
            receivedContextSize
        )

        assertSame(
            expectedResult,
            result
        )
    }

    @Test
    fun prepare_preservesFailureDetails() {
        val expectedResult =
            CompactRuntimePreparationResult
                .Failure(
                    details =
                        "PATH/MODEL_FILE_NOT_FOUND: modello assente"
                )

        val lifecycle =
            AndroidCompactRuntimeLifecycle(
                prepareAction = {
                    expectedResult
                },
                releaseAction = {},
                testOnlyMarker = Unit
            )

        val result =
            lifecycle.prepare(
                contextSize = 8192
            )

        assertEquals(
            expectedResult,
            result
        )
    }

    @Test
    fun release_delegatesExactlyOnce() {
        var releaseCount = 0

        val lifecycle =
            AndroidCompactRuntimeLifecycle(
                prepareAction = {
                    CompactRuntimePreparationResult
                        .Ready
                },
                releaseAction = {
                    releaseCount += 1
                },
                testOnlyMarker = Unit
            )

        lifecycle.release()

        assertEquals(
            1,
            releaseCount
        )
    }

    @Test
    fun release_propagatesUnexpectedException() {
        val lifecycle =
            AndroidCompactRuntimeLifecycle(
                prepareAction = {
                    CompactRuntimePreparationResult
                        .Ready
                },
                releaseAction = {
                    throw IllegalStateException(
                        "rilascio fallito"
                    )
                },
                testOnlyMarker = Unit
            )

        val exception =
            try {
                lifecycle.release()
                null
            } catch (
                caught: IllegalStateException
            ) {
                caught
            }

        requireNotNull(exception)

        assertTrue(
            exception.message.orEmpty()
                .contains(
                    "rilascio fallito"
                )
        )
    }
}