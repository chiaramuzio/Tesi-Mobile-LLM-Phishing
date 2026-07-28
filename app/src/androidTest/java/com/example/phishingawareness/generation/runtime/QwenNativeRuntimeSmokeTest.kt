package com.example.phishingawareness.generation.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QwenNativeRuntimeSmokeTest {

    @Test
    fun qwenQ8_realModel_loadsTemplatesAndGeneratesShortSequence() {
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

            assertTrue(
                NativeRuntimeBridge.isModelLoaded()
            )

            assertEquals(
                "OK|CONTEXT_CREATED",
                NativeRuntimeBridge.createContext(
                    contextSize = SMOKE_CONTEXT_SIZE
                )
            )

            assertTrue(
                NativeRuntimeBridge.isContextReady()
            )

            val runtimeInfo =
                NativeRuntimeBridge.contextRuntimeInfo()

            assertTrue(
                runtimeInfo,
                runtimeInfo.startsWith(
                    "OK|CONTEXT_RUNTIME_INFO|"
                )
            )

            val result =
                NativeRuntimeBridge.generateConfiguredSequenceResult(
                    prompt = SMOKE_PROMPT,
                    addSpecial = true,
                    maxGeneratedTokens = SMOKE_MAX_GENERATED_TOKENS,
                    temperature = 0.4f,
                    topK = 40,
                    topP = 0.90f,
                    minP = 0.05f,
                    repeatPenalty = 1.05f,
                    seed = 101
                )

            assertEquals(
                SMOKE_MAX_GENERATED_TOKENS,
                result.requestedTokenCount
            )

            assertTrue(
                result.generatedTokenCount > 0
            )

            assertTrue(
                result.generatedTokenCount <=
                    SMOKE_MAX_GENERATED_TOKENS
            )

            assertEquals(
                result.generatedTokenCount,
                result.tokenIds.size
            )

            assertTrue(
                result.tokenIds.all { tokenId ->
                    tokenId >= 0
                }
            )

            assertFalse(
                result.rawText.isBlank()
            )

            println(
                "QWEN_SMOKE_RESULT|" +
                        "requestedTokens=${result.requestedTokenCount}|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawText=${result.rawText}"
            )

            val containsThinkOpenTag =
                result.rawText.contains(
                    "<think>",
                    ignoreCase = true
                )

            val containsThinkCloseTag =
                result.rawText.contains(
                    "</think>",
                    ignoreCase = true
                )

            println(
                "QWEN_SMOKE_THINK_WRAPPER|" +
                        "open=$containsThinkOpenTag|" +
                        "close=$containsThinkCloseTag"
            )

            println(
                "QWEN_SMOKE_MODEL_PATH|" +
                    modelFile.absolutePath
            )

            println(
                "QWEN_SMOKE_CONTEXT_INFO|" +
                    runtimeInfo
            )

        } finally {
            releaseNativeResources()
        }
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

        const val SMOKE_CONTEXT_SIZE =
            2_048

        const val SMOKE_MAX_GENERATED_TOKENS =
            64

        const val SMOKE_PROMPT =
            "/no_think\n" +
                "Rispondi soltanto con questa frase esatta: " +
                "OK QWEN ANDROID"
    }
}