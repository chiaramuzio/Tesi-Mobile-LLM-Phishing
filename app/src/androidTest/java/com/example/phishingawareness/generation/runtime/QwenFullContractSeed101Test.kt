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
class QwenFullContractSeed101Test {

    @Test
    fun accountItFullContract_seed101_generatesNativeRawOutput() {
        val instrumentation =
            InstrumentationRegistry.getInstrumentation()

        val applicationContext =
            instrumentation.targetContext

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

        val prompt =
            instrumentation.context.assets
                .open(PROMPT_ASSET_PATH)
                .bufferedReader(Charsets.UTF_8)
                .use { reader ->
                    reader.readText()
                }

        assertTrue(
            "Il prompt congelato è vuoto.",
            prompt.isNotBlank()
        )

        releaseNativeResources()

        try {
            val pssBeforeLoadKb =
                currentProcessPssKb()

            val loadStartedAt =
                SystemClock.elapsedRealtime()

            assertEquals(
                "OK|MODEL_LOADED",
                NativeRuntimeBridge.loadModel(
                    modelPath = modelFile.absolutePath
                )
            )

            val loadMilliseconds =
                SystemClock.elapsedRealtime() -
                        loadStartedAt

            val contextStartedAt =
                SystemClock.elapsedRealtime()

            assertEquals(
                "OK|CONTEXT_CREATED",
                NativeRuntimeBridge.createContext(
                    contextSize = CONTEXT_SIZE
                )
            )

            val contextMilliseconds =
                SystemClock.elapsedRealtime() -
                        contextStartedAt

            val generationStartedAt =
                SystemClock.elapsedRealtime()

            val result =
                NativeRuntimeBridge.generateConfiguredSequenceResult(
                    prompt = prompt,
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

            assertTrue(
                result.generatedTokenCount > 0
            )

            assertTrue(
                result.generatedTokenCount <=
                        MAX_GENERATED_TOKENS
            )

            assertTrue(
                result.rawText.isNotBlank()
            )

            val resultDirectory =
                requireNotNull(
                    applicationContext.getExternalFilesDir(
                        "qwen-results"
                    )
                )

            val resultFile =
                File(
                    resultDirectory,
                    "QWEN3_1_7B_Q8_0_ANDROID_FULL_CONTRACT_v2_seed101_result.txt"
                )

            resultFile.writeText(
                buildString {
                    appendLine(
                        "QWEN_FULL_CONTRACT_METRICS|" +
                                "scenario=ACCOUNT_IT_01|" +
                                "seed=101|" +
                                "contextSize=$CONTEXT_SIZE|" +
                                "requestedTokens=$MAX_GENERATED_TOKENS|" +
                                "generatedTokens=${result.generatedTokenCount}|" +
                                "eog=${result.reachedEndOfGeneration}|" +
                                "loadMs=$loadMilliseconds|" +
                                "contextMs=$contextMilliseconds|" +
                                "generationCallMs=$generationMilliseconds|" +
                                "pssBeforeLoadKb=$pssBeforeLoadKb|" +
                                "pssAfterGenerationKb=${currentProcessPssKb()}"
                    )

                    appendLine(
                        "QWEN_FULL_CONTRACT_RAW_BEGIN"
                    )

                    appendLine(
                        result.rawText
                    )

                    appendLine(
                        "QWEN_FULL_CONTRACT_RAW_END"
                    )
                },
                Charsets.UTF_8
            )

            println(
                "QWEN_FULL_CONTRACT_RESULT_FILE|" +
                        resultFile.absolutePath
            )

            println(
                "QWEN_FULL_CONTRACT_METRICS|" +
                        "scenario=ACCOUNT_IT_01|" +
                        "seed=101|" +
                        "contextSize=$CONTEXT_SIZE|" +
                        "requestedTokens=$MAX_GENERATED_TOKENS|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "loadMs=$loadMilliseconds|" +
                        "contextMs=$contextMilliseconds|" +
                        "generationCallMs=$generationMilliseconds|" +
                        "pssBeforeLoadKb=$pssBeforeLoadKb|" +
                        "pssAfterGenerationKb=${currentProcessPssKb()}"
            )

            println(
                "QWEN_FULL_CONTRACT_RAW_BEGIN"
            )

            println(
                result.rawText
            )

            println(
                "QWEN_FULL_CONTRACT_RAW_END"
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

        const val PROMPT_ASSET_PATH =
            "qwen/ACCOUNT_IT_01/" +
                    "ACCOUNT_IT_01_ZERO_SHOT_QWEN3_1_7B_FULL_CONTRACT_v2.txt"

        const val CONTEXT_SIZE =
            8_192

        const val MAX_GENERATED_TOKENS =
            1_200
    }
}