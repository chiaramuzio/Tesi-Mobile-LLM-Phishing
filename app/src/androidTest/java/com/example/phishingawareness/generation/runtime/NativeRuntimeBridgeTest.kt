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
    fun nativeVersion_greedySequenceEnabled_returnsExpectedVersion() {
        assertEquals(
            "phishingawareness-native-9-greedy-sequence",
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

    @Test
    fun nativeTokenization_realGemmaModel_tokenizesUtf8Prompt() {
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
            NativeRuntimeBridge.tokenizePrompt(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "ERROR|CONTEXT_NOT_CREATED",
            NativeRuntimeBridge.tokenizePrompt(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|CONTEXT_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "ERROR|PROMPT_EMPTY",
            NativeRuntimeBridge.tokenizePrompt(
                prompt = "",
                addSpecial = true
            )
        )

        val plainResult =
            NativeRuntimeBridge.tokenizePrompt(
                prompt = "Ciao, come stai?",
                addSpecial = false
            )

        val specialResult =
            NativeRuntimeBridge.tokenizePrompt(
                prompt = "Ciao, come stai?",
                addSpecial = true
            )

        val utf8Result =
            NativeRuntimeBridge.tokenizePrompt(
                prompt =
                    "È possibile verificare l’account aziendale?",
                addSpecial = true
            )

        assertTrue(
            plainResult.startsWith(
                "OK|TOKEN_COUNT|"
            )
        )

        assertTrue(
            specialResult.startsWith(
                "OK|TOKEN_COUNT|"
            )
        )

        assertTrue(
            utf8Result.startsWith(
                "OK|TOKEN_COUNT|"
            )
        )

        val plainTokenCount =
            plainResult
                .substringAfterLast("|")
                .toInt()

        val specialTokenCount =
            specialResult
                .substringAfterLast("|")
                .toInt()

        val utf8TokenCount =
            utf8Result
                .substringAfterLast("|")
                .toInt()

        assertTrue(
            plainTokenCount > 0
        )

        assertTrue(
            specialTokenCount >= plainTokenCount
        )

        assertTrue(
            utf8TokenCount > 0
        )

        assertTrue(
            utf8TokenCount <= 2_048
        )

        assertEquals(
            "OK|CONTEXT_FREED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }

    @Test
    fun nativePromptDecode_realGemmaModel_processesPromptAndClearsContext() {
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
            NativeRuntimeBridge.decodePromptProbe(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "ERROR|CONTEXT_NOT_CREATED",
            NativeRuntimeBridge.decodePromptProbe(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|CONTEXT_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "ERROR|PROMPT_EMPTY",
            NativeRuntimeBridge.decodePromptProbe(
                prompt = "",
                addSpecial = true
            )
        )

        val result =
            NativeRuntimeBridge.decodePromptProbe(
                prompt =
                    "Rispondi in italiano con una sola parola: ciao.",
                addSpecial = true
            )

        assertTrue(
            result.startsWith(
                "OK|PROMPT_DECODED|TOKEN_COUNT|"
            )
        )

        val tokenCount =
            result
                .substringAfterLast("|")
                .toInt()

        assertTrue(
            tokenCount > 0
        )

        assertTrue(
            tokenCount <= 2_048
        )

        val secondResult =
            NativeRuntimeBridge.decodePromptProbe(
                prompt =
                    "Verifica controllata del secondo prompt.",
                addSpecial = true
            )

        assertTrue(
            secondResult.startsWith(
                "OK|PROMPT_DECODED|TOKEN_COUNT|"
            )
        )

        assertEquals(
            "OK|CONTEXT_FREED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }

    @Test
    fun nativeFirstToken_realGemmaModel_generatesGreedyToken() {
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
            NativeRuntimeBridge.generateFirstTokenGreedy(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "ERROR|CONTEXT_NOT_CREATED",
            NativeRuntimeBridge.generateFirstTokenGreedy(
                prompt = "Ciao",
                addSpecial = true
            )
        )

        assertEquals(
            "OK|CONTEXT_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "ERROR|PROMPT_EMPTY",
            NativeRuntimeBridge.generateFirstTokenGreedy(
                prompt = "",
                addSpecial = true
            )
        )

        val result =
            NativeRuntimeBridge.generateFirstTokenGreedy(
                prompt =
                    "Rispondi in italiano con una sola parola: ciao.",
                addSpecial = true
            )

        assertTrue(
            result,
            result.startsWith(
                "OK|FIRST_TOKEN|TOKEN_ID|"
            )
        )

        val tokenId =
            result
                .substringAfter("|TOKEN_ID|")
                .substringBefore("|EOG|")
                .toInt()

        val eogFlag =
            result
                .substringAfter("|EOG|")
                .substringBefore("|PIECE_HEX|")

        val pieceHex =
            result.substringAfter(
                "|PIECE_HEX|"
            )

        assertTrue(
            tokenId >= 0
        )

        assertTrue(
            eogFlag == "0" ||
                    eogFlag == "1"
        )

        assertTrue(
            pieceHex.length % 2 == 0
        )

        if (eogFlag == "0") {
            assertTrue(
                pieceHex.isNotEmpty()
            )
        }

        assertEquals(
            "OK|CONTEXT_FREED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }

    @Test
    fun nativeGreedySequence_realGemmaModel_generatesControlledSequence() {
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
            "ERROR|INVALID_MAX_GENERATED_TOKENS",
            NativeRuntimeBridge.generateGreedySequence(
                prompt = "Ciao",
                addSpecial = true,
                maxGeneratedTokens = 0
            )
        )

        assertEquals(
            "ERROR|INVALID_MAX_GENERATED_TOKENS",
            NativeRuntimeBridge.generateGreedySequence(
                prompt = "Ciao",
                addSpecial = true,
                maxGeneratedTokens = 9
            )
        )

        assertEquals(
            "ERROR|MODEL_NOT_LOADED",
            NativeRuntimeBridge.generateGreedySequence(
                prompt = "Ciao",
                addSpecial = true,
                maxGeneratedTokens = 4
            )
        )

        assertEquals(
            "OK|MODEL_LOADED",
            NativeRuntimeBridge.loadModel(
                modelPath = modelFile.absolutePath
            )
        )

        assertEquals(
            "ERROR|CONTEXT_NOT_CREATED",
            NativeRuntimeBridge.generateGreedySequence(
                prompt = "Ciao",
                addSpecial = true,
                maxGeneratedTokens = 4
            )
        )

        assertEquals(
            "OK|CONTEXT_CREATED",
            NativeRuntimeBridge.createContext(
                contextSize = 2_048
            )
        )

        assertEquals(
            "ERROR|PROMPT_EMPTY",
            NativeRuntimeBridge.generateGreedySequence(
                prompt = "",
                addSpecial = true,
                maxGeneratedTokens = 4
            )
        )

        val result =
            NativeRuntimeBridge.generateGreedySequence(
                prompt =
                    "Rispondi in italiano con una frase molto breve: ciao.",
                addSpecial = true,
                maxGeneratedTokens = 4
            )

        assertTrue(
            result,
            result.startsWith(
                "OK|GREEDY_SEQUENCE|"
            )
        )

        assertTrue(
            result,
            result.contains(
                "|REQUESTED_TOKEN_COUNT|4|"
            )
        )

        val generatedTokenCount =
            result
                .substringAfter(
                    "|GENERATED_TOKEN_COUNT|"
                )
                .substringBefore(
                    "|EOG|"
                )
                .toInt()

        val eogFlag =
            result
                .substringAfter(
                    "|EOG|"
                )
                .substringBefore(
                    "|TOKEN_IDS|"
                )

        val tokenIdsText =
            result
                .substringAfter(
                    "|TOKEN_IDS|"
                )
                .substringBefore(
                    "|OUTPUT_HEX|"
                )

        val outputHex =
            result.substringAfter(
                "|OUTPUT_HEX|"
            )

        assertTrue(
            generatedTokenCount in 1..4
        )

        assertTrue(
            eogFlag == "0" ||
                    eogFlag == "1"
        )

        val tokenIds =
            tokenIdsText
                .split(",")
                .map(String::toInt)

        assertEquals(
            generatedTokenCount,
            tokenIds.size
        )

        assertTrue(
            tokenIds.all { it >= 0 }
        )

        assertTrue(
            outputHex.length % 2 == 0
        )

        assertEquals(
            "OK|CONTEXT_FREED",
            NativeRuntimeBridge.freeContext()
        )

        assertEquals(
            "OK|MODEL_UNLOADED",
            NativeRuntimeBridge.unloadModel()
        )
    }
}