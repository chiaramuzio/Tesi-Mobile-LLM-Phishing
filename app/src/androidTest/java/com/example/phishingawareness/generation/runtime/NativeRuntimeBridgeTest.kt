package com.example.phishingawareness.generation.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

@RunWith(AndroidJUnit4::class)
class NativeRuntimeBridgeTest {

    @Test
    fun nativeVersion_inferenceContextEnabled_returnsExpectedVersion() {
        assertEquals(
            "phishingawareness-native-5-inference-context",
            NativeRuntimeBridge.nativeVersion()
        )
    }

    @Test
    fun llamaMaxDevices_llamaCppLinked_returnsPositiveValue() {
        val maxDevices =
            NativeRuntimeBridge.llamaMaxDevices()

        assertTrue(
            maxDevices > 0
        )
    }

    @Test
    fun llamaSupportsMmap_llamaCppLinked_canBeCalled() {
        NativeRuntimeBridge.llamaSupportsMmap()
    }

    @Test
    fun loadModelProbe_emptyPath_returnsControlledFailure() {
        assertEquals(
            "ERROR|MODEL_PATH_EMPTY",
            NativeRuntimeBridge.loadModelProbe(
                modelPath = ""
            )
        )
    }

    @Test
    fun loadModelProbe_missingFile_returnsControlledFailure() {
        assertEquals(
            "ERROR|MODEL_LOAD_FAILED",
            NativeRuntimeBridge.loadModelProbe(
                modelPath =
                    "/data/local/tmp/model-that-does-not-exist.gguf"
            )
        )
    }

    @Test
    fun loadModelProbe_realGemmaModel_loadsAndReleasesModel() {
        val applicationContext =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val modelsDirectory =
            requireNotNull(
                applicationContext.getExternalFilesDir(
                    "models"
                )
            )

        val modelFile =
            File(
                modelsDirectory,
                "gemma-3-4b-it-q4_0.gguf"
            )

        assertTrue(
            "Modello GGUF non trovato: ${modelFile.absolutePath}",
            modelFile.isFile
        )

        assertEquals(
            3_155_051_328L,
            modelFile.length()
        )

        val result =
            NativeRuntimeBridge.loadModelProbe(
                modelPath = modelFile.absolutePath
            )

        assertEquals(
            "OK|MODEL_LOADED_AND_RELEASED",
            result
        )
    }

    @Test
    fun persistentModelSession_realGemmaModel_loadsTracksAndUnloads() {
        val applicationContext =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val modelsDirectory =
            requireNotNull(
                applicationContext.getExternalFilesDir(
                    "models"
                )
            )

        val modelFile =
            File(
                modelsDirectory,
                "gemma-3-4b-it-q4_0.gguf"
            )

        assertTrue(
            "Modello GGUF non trovato: ${modelFile.absolutePath}",
            modelFile.isFile
        )

        assertEquals(
            3_155_051_328L,
            modelFile.length()
        )

        if (NativeRuntimeBridge.isModelLoaded()) {
            assertEquals(
                "OK|MODEL_UNLOADED",
                NativeRuntimeBridge.unloadModel()
            )
        }

        assertEquals(
            false,
            NativeRuntimeBridge.isModelLoaded()
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertTrue(
            NativeRuntimeBridge.isModelLoaded()
        )

        assertEquals(
            "ERROR|MODEL_ALREADY_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )

        assertEquals(
            false,
            NativeRuntimeBridge.isModelLoaded()
        )

        assertEquals(
            "ERROR|MODEL_NOT_LOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }

    @Test
    fun persistentInferenceContext_realGemmaModel_createsTracksAndFreesContext() {
        val applicationContext =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val modelsDirectory =
            requireNotNull(
                applicationContext.getExternalFilesDir(
                    "models"
                )
            )

        val modelFile =
            File(
                modelsDirectory,
                "gemma-3-4b-it-q4_0.gguf"
            )

        assertTrue(
            "Modello GGUF non trovato: ${modelFile.absolutePath}",
            modelFile.isFile
        )

        if (NativeRuntimeBridge.isContextReady()) {
            assertEquals(
                "OK|CONTEXT_FREED",
                NativeRuntimeBridge.freeContext()
            )
        }

        if (NativeRuntimeBridge.isModelLoaded()) {
            assertEquals(
                "OK|MODEL_UNLOADED",
                NativeRuntimeBridge.unloadModel()
            )
        }

        assertEquals(
            "ERROR|MODEL_NOT_LOADED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "ERROR|CONTEXT_SIZE_INVALID",
            NativeRuntimeBridge.createContext(
                contextSize = 0
            )
        )

        assertEquals(
            "OK|CONTEXT_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertTrue(
            NativeRuntimeBridge.isContextReady()
        )

        assertEquals(
            2_048L,
            NativeRuntimeBridge.contextSize()
        )

        assertEquals(
            "ERROR|CONTEXT_ALREADY_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "ERROR|CONTEXT_MUST_BE_FREED",
            NativeRuntimeBridge.unloadModel()
        )

        assertEquals(
            "OK|CONTEXT_FREED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            false,
            NativeRuntimeBridge.isContextReady()
        )

        assertEquals(
            0L,
            NativeRuntimeBridge.contextSize()
        )

        assertEquals(
            "ERROR|CONTEXT_NOT_CREATED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }
}