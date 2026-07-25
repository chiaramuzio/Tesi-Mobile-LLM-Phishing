package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicLocalModelSessionTest {

    @Test
    fun prepare_emptyModelPath_returnsValidationFailure() {
        val gateway =
            FakeNativeModelSessionGateway()

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = " ",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Failure

        assertEquals(
            LocalModelSessionFailureStage
                .REQUEST_VALIDATION,
            result.stage
        )

        assertEquals(
            LocalModelSessionFailureCode
                .MODEL_PATH_EMPTY,
            result.code
        )

        assertEquals(
            0,
            gateway.loadModelCalls
        )
    }

    @Test
    fun prepare_invalidContextSize_returnsValidationFailure() {
        val gateway =
            FakeNativeModelSessionGateway()

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 0
            )

        result as LocalModelSessionResult.Failure

        assertEquals(
            LocalModelSessionFailureCode
                .INVALID_CONTEXT_SIZE,
            result.code
        )

        assertEquals(
            0,
            gateway.loadModelCalls
        )
    }

    @Test
    fun prepare_newSession_loadsModelAndCreatesContext() {
        val gateway =
            FakeNativeModelSessionGateway()

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Ready

        assertFalse(
            result.modelAlreadyLoaded
        )

        assertFalse(
            result.contextAlreadyReady
        )

        assertEquals(
            8192,
            result.contextSize
        )

        assertEquals(
            1,
            gateway.loadModelCalls
        )

        assertEquals(
            1,
            gateway.createContextCalls
        )

        assertTrue(
            session.isReady()
        )
    }

    @Test
    fun prepare_existingCompatibleSession_reusesResources() {
        val gateway =
            FakeNativeModelSessionGateway(
                modelLoaded = true,
                contextReady = true,
                currentContextSize = 8192
            )

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Ready

        assertTrue(
            result.modelAlreadyLoaded
        )

        assertTrue(
            result.contextAlreadyReady
        )

        assertEquals(
            0,
            gateway.loadModelCalls
        )

        assertEquals(
            0,
            gateway.createContextCalls
        )
    }

    @Test
    fun prepare_existingContextWithDifferentSize_returnsMismatch() {
        val gateway =
            FakeNativeModelSessionGateway(
                modelLoaded = true,
                contextReady = true,
                currentContextSize = 4096
            )

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Failure

        assertEquals(
            LocalModelSessionFailureCode
                .CONTEXT_SIZE_MISMATCH,
            result.code
        )

        assertTrue(
            gateway.contextReady
        )

        assertTrue(
            gateway.modelLoaded
        )
    }

    @Test
    fun prepare_modelLoadFailure_returnsControlledFailure() {
        val gateway =
            FakeNativeModelSessionGateway(
                loadResponse =
                    "ERROR|MODEL_LOAD_FAILED"
            )

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Failure

        assertEquals(
            LocalModelSessionFailureStage
                .MODEL_LOADING,
            result.stage
        )

        assertEquals(
            LocalModelSessionFailureCode
                .MODEL_LOAD_FAILED,
            result.code
        )

        assertEquals(
            0,
            gateway.createContextCalls
        )
    }

    @Test
    fun prepare_contextCreationFailure_rollsBackNewModel() {
        val gateway =
            FakeNativeModelSessionGateway(
                createResponse =
                    "ERROR|CONTEXT_CREATE_FAILED"
            )

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.prepare(
                modelPath = "/models/model.gguf",
                contextSize = 8192
            )

        result as LocalModelSessionResult.Failure

        assertEquals(
            LocalModelSessionFailureCode
                .CONTEXT_CREATE_FAILED,
            result.code
        )

        assertEquals(
            1,
            gateway.unloadModelCalls
        )

        assertFalse(
            gateway.modelLoaded
        )
    }

    @Test
    fun release_readySession_freesContextBeforeModel() {
        val gateway =
            FakeNativeModelSessionGateway(
                modelLoaded = true,
                contextReady = true,
                currentContextSize = 8192
            )

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.release()

        assertEquals(
            LocalModelSessionResult.Released,
            result
        )

        assertEquals(
            listOf(
                "freeContext",
                "unloadModel"
            ),
            gateway.releaseOperations
        )

        assertFalse(
            gateway.contextReady
        )

        assertFalse(
            gateway.modelLoaded
        )
    }

    @Test
    fun release_emptySession_returnsReleased() {
        val gateway =
            FakeNativeModelSessionGateway()

        val session =
            DeterministicLocalModelSession(
                gateway = gateway
            )

        val result =
            session.release()

        assertEquals(
            LocalModelSessionResult.Released,
            result
        )

        assertTrue(
            gateway.releaseOperations.isEmpty()
        )
    }

    private class FakeNativeModelSessionGateway(
        var modelLoaded: Boolean = false,
        var contextReady: Boolean = false,
        var currentContextSize: Long = 0,
        private val loadResponse: String =
            "OK|MODEL_LOADED",
        private val createResponse: String =
            "OK|CONTEXT_CREATED",
        private val freeResponse: String =
            "OK|CONTEXT_FREED",
        private val unloadResponse: String =
            "OK|MODEL_UNLOADED"
    ) : NativeModelSessionGateway {

        var loadModelCalls: Int = 0
        var createContextCalls: Int = 0
        var freeContextCalls: Int = 0
        var unloadModelCalls: Int = 0

        val releaseOperations =
            mutableListOf<String>()

        override fun loadModel(
            modelPath: String
        ): String {
            loadModelCalls += 1

            if (loadResponse == "OK|MODEL_LOADED") {
                modelLoaded = true
            }

            return loadResponse
        }

        override fun isModelLoaded(): Boolean {
            return modelLoaded
        }

        override fun unloadModel(): String {
            unloadModelCalls += 1
            releaseOperations += "unloadModel"

            if (unloadResponse == "OK|MODEL_UNLOADED") {
                modelLoaded = false
            }

            return unloadResponse
        }

        override fun createContext(
            contextSize: Int
        ): String {
            createContextCalls += 1

            if (createResponse == "OK|CONTEXT_CREATED") {
                contextReady = true
                currentContextSize =
                    contextSize.toLong()
            }

            return createResponse
        }

        override fun isContextReady(): Boolean {
            return contextReady
        }

        override fun contextSize(): Long {
            return currentContextSize
        }

        override fun freeContext(): String {
            freeContextCalls += 1
            releaseOperations += "freeContext"

            if (freeResponse == "OK|CONTEXT_FREED") {
                contextReady = false
                currentContextSize = 0
            }

            return freeResponse
        }
    }
}