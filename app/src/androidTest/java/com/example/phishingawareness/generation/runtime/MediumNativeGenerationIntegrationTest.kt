package com.example.phishingawareness.generation.runtime

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrapResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediumNativeGenerationIntegrationTest {

    @Test
    fun generate_realModelWithControlledPrompt_returnsReadableText() {
        val context =
            ApplicationProvider
                .getApplicationContext<Context>()

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
                    prompt = TEST_PROMPT,
                    addSpecial = true,
                    maxGeneratedTokens = MAX_GENERATED_TOKENS,
                    temperature = 0.4f,
                    topK = 40,
                    topP = 0.90f,
                    minP = 0.05f,
                    repeatPenalty = 1.05f,
                    seed = 101
                )

            val elapsedMilliseconds =
                SystemClock.elapsedRealtime() -
                        startedAt

            logChunked(
                marker = "MEDIUM_NATIVE_PROTOCOL",
                value = nativeProtocol
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
                "MEDIUM_NATIVE_TOKEN_IDS|" +
                        "count=${result.tokenIds.size}|" +
                        "values=${result.tokenIds.joinToString(",")}"
            )

            println(
                "MEDIUM_NATIVE_RESULT|" +
                        "elapsedMs=$elapsedMilliseconds|" +
                        "requestedTokens=${result.requestedTokenCount}|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawTextLength=${result.rawText.length}|" +
                        "escapedRawText=$escapedRawText"
            )

            assertTrue(
                "Il modello ha restituito testo vuoto.",
                result.rawText.isNotBlank()
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
                "Il numero degli ID token non corrisponde " +
                        "al numero dichiarato.",
                result.generatedTokenCount,
                result.tokenIds.size
            )

            assertFalse(
                "Il raw text contiene ancora il token EOG.",
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
        const val CONTEXT_SIZE = 8192
        const val MAX_GENERATED_TOKENS = 32
        const val LOG_CHUNK_SIZE = 1500

        const val TEST_PROMPT =
            "Scrivi in italiano esattamente tre frasi brevi. " +
                    "La prima deve salutare l'utente, " +
                    "la seconda deve augurargli una buona giornata " +
                    "e la terza deve congedarsi gentilmente. " +
                    "Non usare elenchi, titoli o blocchi di codice."
    }
}