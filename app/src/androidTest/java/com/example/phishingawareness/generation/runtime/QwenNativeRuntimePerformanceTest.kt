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
class QwenNativeRuntimePerformanceTest {

    @Test
    fun qwenQ8_realModel_recordsLoadContextAndGenerationTimings() {
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
            val memoryBeforeLoadKb =
                currentProcessPssKb()

            val loadStartedAt =
                SystemClock.elapsedRealtime()

            val loadResult =
                NativeRuntimeBridge.loadModel(
                    modelPath = modelFile.absolutePath
                )

            val loadMilliseconds =
                SystemClock.elapsedRealtime() -
                        loadStartedAt

            assertEquals(
                "OK|MODEL_LOADED",
                loadResult
            )

            val memoryAfterLoadKb =
                currentProcessPssKb()

            val contextStartedAt =
                SystemClock.elapsedRealtime()

            val contextResult =
                NativeRuntimeBridge.createContext(
                    contextSize = CONTEXT_SIZE
                )

            val contextMilliseconds =
                SystemClock.elapsedRealtime() -
                        contextStartedAt

            assertEquals(
                "OK|CONTEXT_CREATED",
                contextResult
            )

            val memoryAfterContextKb =
                currentProcessPssKb()

            val generationStartedAt =
                SystemClock.elapsedRealtime()

            val generationResult =
                NativeRuntimeBridge.generateConfiguredSequenceResult(
                    prompt = PROMPT,
                    addSpecial = true,
                    maxGeneratedTokens = MAX_GENERATED_TOKENS,
                    temperature = 0.4f,
                    topK = 40,
                    topP = 0.90f,
                    minP = 0.05f,
                    repeatPenalty = 1.05f,
                    seed = 101
                )

            val generationMilliseconds =
                SystemClock.elapsedRealtime() -
                        generationStartedAt

            val memoryAfterGenerationKb =
                currentProcessPssKb()

            assertTrue(
                generationResult.generatedTokenCount > 0
            )

            assertTrue(
                generationResult.generatedTokenCount <=
                        MAX_GENERATED_TOKENS
            )

            assertTrue(
                generationResult.rawText.isNotBlank()
            )

            println(
                "QWEN_PERFORMANCE|" +
                        "model=Qwen3-1.7B-Q8_0|" +
                        "device=HUAWEI_MAR-LX1B|" +
                        "contextSize=$CONTEXT_SIZE|" +
                        "requestedTokens=$MAX_GENERATED_TOKENS|" +
                        "generatedTokens=${generationResult.generatedTokenCount}|" +
                        "eog=${generationResult.reachedEndOfGeneration}|" +
                        "loadMs=$loadMilliseconds|" +
                        "contextMs=$contextMilliseconds|" +
                        "generationCallMs=$generationMilliseconds|" +
                        "pssBeforeLoadKb=$memoryBeforeLoadKb|" +
                        "pssAfterLoadKb=$memoryAfterLoadKb|" +
                        "pssAfterContextKb=$memoryAfterContextKb|" +
                        "pssAfterGenerationKb=$memoryAfterGenerationKb"
            )

            println(
                "QWEN_PERFORMANCE_RAW|" +
                        generationResult.rawText
            )
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

        const val CONTEXT_SIZE =
            2_048

        const val MAX_GENERATED_TOKENS =
            64

        const val PROMPT =
            "/no_think\n" +
                    "Rispondi soltanto con questa frase esatta: " +
                    "OK QWEN ANDROID"
    }
}