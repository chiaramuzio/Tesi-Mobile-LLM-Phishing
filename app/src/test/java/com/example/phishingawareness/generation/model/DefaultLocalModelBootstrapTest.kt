package com.example.phishingawareness.generation.model

import com.example.phishingawareness.generation.runtime.LocalModelSession
import com.example.phishingawareness.generation.runtime.LocalModelSessionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultLocalModelBootstrapTest {

    @Test
    fun prepare_availableModel_preparesSession() {
        val pathProvider =
            LocalModelPathProvider {
                LocalModelPathResult.Available(
                    absolutePath =
                        "/models/gemma.gguf",
                    sizeBytes =
                        3_155_051_328L
                )
            }

        val session =
            FakeLocalModelSession()

        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider = pathProvider,
                session = session
            )

        val result =
            bootstrap.prepare(
                contextSize = 8192
            )

        result as LocalModelBootstrapResult.Ready

        assertEquals(
            "/models/gemma.gguf",
            session.receivedModelPath
        )

        assertEquals(
            8192,
            session.receivedContextSize
        )

        assertEquals(
            3_155_051_328L,
            result.modelSizeBytes
        )

        assertTrue(
            bootstrap.isReady()
        )
    }

    @Test
    fun prepare_unavailableModel_doesNotPrepareSession() {
        val pathFailure =
            LocalModelPathResult.Unavailable(
                code =
                    LocalModelPathFailureCode
                        .MODEL_FILE_NOT_FOUND,
                expectedPath =
                    "/models/gemma.gguf"
            )

        val pathProvider =
            LocalModelPathProvider {
                pathFailure
            }

        val session =
            FakeLocalModelSession()

        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider = pathProvider,
                session = session
            )

        val result =
            bootstrap.prepare(
                contextSize = 8192
            )

        result as LocalModelBootstrapResult.PathFailure

        assertEquals(
            pathFailure,
            result.failure
        )

        assertEquals(
            0,
            session.prepareCalls
        )

        assertFalse(
            bootstrap.isReady()
        )
    }

    @Test
    fun prepare_sessionFailure_returnsControlledFailure() {
        val pathProvider =
            LocalModelPathProvider {
                LocalModelPathResult.Available(
                    absolutePath =
                        "/models/gemma.gguf",
                    sizeBytes =
                        3_155_051_328L
                )
            }

        val sessionFailure =
            LocalModelSessionResult.Failure(
                stage =
                    com.example.phishingawareness
                        .generation.runtime
                        .LocalModelSessionFailureStage
                        .MODEL_LOADING,
                code =
                    com.example.phishingawareness
                        .generation.runtime
                        .LocalModelSessionFailureCode
                        .MODEL_LOAD_FAILED,
                details =
                    "ERROR|MODEL_LOAD_FAILED"
            )

        val session =
            FakeLocalModelSession(
                prepareResult = sessionFailure
            )

        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider = pathProvider,
                session = session
            )

        val result =
            bootstrap.prepare(
                contextSize = 8192
            )

        result as LocalModelBootstrapResult.SessionFailure

        assertEquals(
            sessionFailure,
            result.failure
        )
    }

    @Test
    fun release_forwardsToSession() {
        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider =
                    LocalModelPathProvider {
                        error("Non atteso")
                    },
                session =
                    FakeLocalModelSession(
                        ready = true
                    )
            )

        val result =
            bootstrap.release()

        assertEquals(
            LocalModelSessionResult.Released,
            result
        )

        assertFalse(
            bootstrap.isReady()
        )
    }

    private class FakeLocalModelSession(
        private val prepareResult:
        LocalModelSessionResult =
            LocalModelSessionResult.Ready(
                modelAlreadyLoaded = false,
                contextAlreadyReady = false,
                contextSize = 8192
            ),
        ready: Boolean = false
    ) : LocalModelSession {

        var prepareCalls: Int = 0
        var receivedModelPath: String? = null
        var receivedContextSize: Int? = null

        private var currentReady =
            ready

        override fun prepare(
            modelPath: String,
            contextSize: Int
        ): LocalModelSessionResult {
            prepareCalls += 1
            receivedModelPath = modelPath
            receivedContextSize = contextSize

            currentReady =
                prepareResult is
                        LocalModelSessionResult.Ready

            return prepareResult
        }

        override fun release():
                LocalModelSessionResult {
            currentReady = false
            return LocalModelSessionResult.Released
        }

        override fun isReady(): Boolean {
            return currentReady
        }
    }
}