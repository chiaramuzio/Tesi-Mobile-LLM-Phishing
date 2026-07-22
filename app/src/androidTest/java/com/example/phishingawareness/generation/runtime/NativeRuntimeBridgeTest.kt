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
    fun nativeVersion_ggufLoadingEnabled_returnsExpectedVersion() {
        assertEquals(
            "phishingawareness-native-3-gguf-load",
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
}