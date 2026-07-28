package com.example.phishingawareness.generation.runtime

import android.os.Debug
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QwenNativeContextCapacityTest {

    @Test
    fun qwenQ8_realModel_recordsSupportedContextSizes() {
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
                QWEN_MODEL_FILE_NAME
            )

        assertTrue(
            "Modello Qwen GGUF non trovato: ${modelFile.absolutePath}",
            modelFile.isFile
        )

        assertEquals(
            QWEN_MODEL_EXPECTED_SIZE,
            modelFile.length()
        )

        releaseNativeResources()

        try {
            assertEquals(
                "OK|MODEL_LOADED",
                NativeRuntimeBridge.loadModel(
                    modelPath = modelFile.absolutePath
                )
            )

            println(
                "QWEN_CONTEXT_MODEL_LOADED|" +
                        "pssKb=${currentProcessPssKb()}"
            )

            for (contextSize in CONTEXT_SIZES) {
                val startedAt =
                    SystemClock.elapsedRealtime()

                val result =
                    NativeRuntimeBridge.createContext(
                        contextSize = contextSize
                    )

                val elapsedMilliseconds =
                    SystemClock.elapsedRealtime() -
                            startedAt

                println(
                    "QWEN_CONTEXT_CAPACITY|" +
                            "contextSize=$contextSize|" +
                            "result=$result|" +
                            "elapsedMs=$elapsedMilliseconds|" +
                            "pssKb=${currentProcessPssKb()}"
                )

                if (result == "OK|CONTEXT_CREATED") {
                    assertEquals(
                        contextSize.toLong(),
                        NativeRuntimeBridge.contextSize()
                    )

                    assertEquals(
                        "OK|CONTEXT_FREED",
                        NativeRuntimeBridge.freeContext()
                    )
                }
            }
        } finally {
            releaseNativeResources()
        }
    }

    private fun currentProcessPssKb(): Int {
        val memoryInfo =
            Debug.MemoryInfo()

        Debug.getMemoryInfo(
            memoryInfo
        )

        return memoryInfo.totalPss
    }

    private fun releaseNativeResources() {
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
    }

    private companion object {
        const val QWEN_MODEL_FILE_NAME =
            "Qwen3-1.7B-Q8_0.gguf"

        const val QWEN_MODEL_EXPECTED_SIZE =
            1_834_426_016L

        val CONTEXT_SIZES =
            intArrayOf(
                2_048,
                4_096,
                8_192
            )
    }
}