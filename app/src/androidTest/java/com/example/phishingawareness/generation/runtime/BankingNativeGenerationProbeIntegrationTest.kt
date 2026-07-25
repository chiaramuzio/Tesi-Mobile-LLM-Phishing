package com.example.phishingawareness.generation.runtime

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.di.AppContainer
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrapResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankingNativeGenerationProbeIntegrationTest {

    @Test
    fun generate_bankingRuntimePrompt_returnsNativeRawOutput() {
        val context =
            ApplicationProvider
                .getApplicationContext<Context>()

        val appContainer =
            AppContainer(
                context = context
            )

        val promptResult =
            appContainer
                .buildRuntimePromptUseCase(
                    GenerationRequest(
                        scenarioId = "BANKING",
                        difficulty = "MEDIUM",
                        length = "MEDIUM"
                    )
                )

        assertTrue(
            "Costruzione del prompt BANKING fallita: $promptResult",
            promptResult is RuntimePromptGenerationResult.Success
        )

        promptResult as RuntimePromptGenerationResult.Success

        val artifact =
            promptResult.artifact

        println(
            "BANKING_NATIVE_ARTIFACT|" +
                    "profile=" +
                    "${artifact.metadata.resolvedConfigurationId}|" +
                    "rawChars=${artifact.text.length}|" +
                    "sha256=${artifact.metadata.promptSha256}|" +
                    "builderVersion=" +
                    "${artifact.metadata.buildContext.builderVersion}|" +
                    "templateId=" +
                    "${artifact.metadata.buildContext.templateId}|" +
                    "templateVersion=" +
                    "${artifact.metadata.buildContext.templateVersion}|" +
                    "libraryVersion=" +
                    "${artifact.metadata.buildContext.libraryVersion}"
        )

        assertEquals(
            "Profilo runtime inatteso.",
            EXPECTED_PROFILE_ID,
            artifact.metadata.resolvedConfigurationId
        )

        assertTrue(
            "Il prompt runtime BANKING è vuoto.",
            artifact.text.isNotBlank()
        )

        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider =
                    AndroidLocalModelPathProvider(
                        context = context
                    ),
                session =
                    DeterministicLocalModelSession()
            )

        try {
            val prepareResult =
                bootstrap.prepare(
                    contextSize = CONTEXT_SIZE
                )

            assertTrue(
                "Impossibile preparare il modello: $prepareResult",
                prepareResult is LocalModelBootstrapResult.Ready
            )

            val startedAt =
                SystemClock.elapsedRealtime()

            val nativeProtocol =
                NativeRuntimeBridge.generateConfiguredSequence(
                    prompt = artifact.text,
                    addSpecial = true,
                    maxGeneratedTokens = MAX_GENERATED_TOKENS,
                    temperature = TEMPERATURE,
                    topK = TOP_K,
                    topP = TOP_P,
                    minP = MIN_P,
                    repeatPenalty = REPEAT_PENALTY,
                    seed = SEED
                )

            val elapsedMilliseconds =
                SystemClock.elapsedRealtime() -
                        startedAt

            logChunked(
                marker = "BANKING_NATIVE_PROTOCOL",
                value = nativeProtocol
            )

            assertTrue(
                "La generazione nativa ha restituito un errore: " +
                        nativeProtocol.take(300),
                nativeProtocol.startsWith(
                    "OK|GREEDY_SEQUENCE|"
                )
            )

            val result =
                NativeGeneratedSequenceParser.parse(
                    protocol = nativeProtocol
                )

            val escapedRawText =
                escapeForLog(
                    value = result.rawText
                )

            println(
                "BANKING_NATIVE_TOKEN_IDS|" +
                        "count=${result.tokenIds.size}|" +
                        "values=${result.tokenIds.joinToString(",")}"
            )

            println(
                "BANKING_NATIVE_RESULT|" +
                        "elapsedMs=$elapsedMilliseconds|" +
                        "requestedTokens=${result.requestedTokenCount}|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawTextLength=${result.rawText.length}|" +
                        "escapedRawText=$escapedRawText"
            )

            assertEquals(
                "Limite di generazione inatteso.",
                MAX_GENERATED_TOKENS,
                result.requestedTokenCount
            )

            assertTrue(
                "Il modello non ha generato token.",
                result.generatedTokenCount > 0
            )

            assertTrue(
                "Il runtime ha superato il limite richiesto.",
                result.generatedTokenCount <=
                        MAX_GENERATED_TOKENS
            )

            assertEquals(
                "Gli ID token non corrispondono al conteggio dichiarato.",
                result.generatedTokenCount,
                result.tokenIds.size
            )

            assertTrue(
                "Il raw output nativo è vuoto.",
                result.rawText.isNotBlank()
            )

            assertFalse(
                "Il raw output contiene il token EOG.",
                result.rawText.contains(
                    "<end_of_turn>"
                )
            )
        } finally {
            bootstrap.release()

            assertFalse(
                bootstrap.isReady()
            )
        }
    }

    private fun logChunked(
        marker: String,
        value: String
    ) {
        println(
            "${marker}_BEGIN|length=${value.length}"
        )

        value
            .chunked(LOG_CHUNK_SIZE)
            .forEachIndexed { index, chunk ->
                println(
                    "$marker|chunk=$index|$chunk"
                )
            }

        println(
            "${marker}_END"
        )
    }

    private fun escapeForLog(
        value: String
    ): String {
        return buildString {
            value.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")

                    else -> {
                        if (
                            character.code < 0x20 ||
                            character.code == 0x7F
                        ) {
                            append(
                                "\\u%04x".format(
                                    character.code
                                )
                            )
                        } else {
                            append(character)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val EXPECTED_PROFILE_ID =
            "BANKING_MEDIUM_MEDIUM"

        const val CONTEXT_SIZE = 8192
        const val MAX_GENERATED_TOKENS = 64
        const val LOG_CHUNK_SIZE = 1500

        const val TEMPERATURE = 0.4f
        const val TOP_K = 40
        const val TOP_P = 0.90f
        const val MIN_P = 0.05f
        const val REPEAT_PENALTY = 1.05f
        const val SEED = 101
    }
}