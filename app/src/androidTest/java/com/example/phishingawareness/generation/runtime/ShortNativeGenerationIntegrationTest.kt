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

class ShortNativeGenerationIntegrationTest {

    @Test
    fun generate_realModelWithFixedSeed_returnsNonEmptyText() {
        val context =
            ApplicationProvider
                .getApplicationContext<Context>()

        val session =
            DeterministicLocalModelSession()

        val bootstrap =
            DefaultLocalModelBootstrap(
                pathProvider =
                    AndroidLocalModelPathProvider(
                        context = context
                    ),
                session = session
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

            /*
             * Usiamo direttamente il protocollo nativo per questa
             * diagnostica, così la generazione viene eseguita una sola volta
             * e possiamo conservare sia il protocollo originale sia il
             * risultato Kotlin tipizzato.
             */
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
                marker = "SHORT_NATIVE_PROTOCOL",
                value = nativeProtocol
            )

            val outputHex =
                nativeProtocol.substringAfter(
                    delimiter = OUTPUT_HEX_MARKER,
                    missingDelimiterValue = ""
                )

            println(
                "SHORT_NATIVE_OUTPUT_HEX|" +
                        "hexLength=${outputHex.length}|" +
                        "byteLength=${outputHex.length / 2}|" +
                        "value=$outputHex"
            )

            val result =
                NativeGeneratedSequenceParser.parse(
                    protocol = nativeProtocol
                )

            println(
                "SHORT_NATIVE_TOKEN_IDS|" +
                        "count=${result.tokenIds.size}|" +
                        "values=${result.tokenIds.joinToString(",")}"
            )

            val escapedRawText =
                escapeForLog(
                    value = result.rawText
                )

            println(
                "SHORT_NATIVE_RESULT|" +
                        "elapsedMs=$elapsedMilliseconds|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawTextLength=${result.rawText.length}|" +
                        "escapedRawText=$escapedRawText"
            )

            assertTrue(
                "Il runtime ha restituito testo vuoto. " +
                        "Token generati: ${result.generatedTokenCount}; " +
                        "tempo: $elapsedMilliseconds ms.",
                result.rawText.isNotBlank()
            )

            assertTrue(
                "Il runtime non ha generato alcun token.",
                result.generatedTokenCount > 0
            )

            assertTrue(
                "Il runtime ha superato il limite richiesto: " +
                        "${result.generatedTokenCount} > " +
                        "$MAX_GENERATED_TOKENS.",
                result.generatedTokenCount <=
                        MAX_GENERATED_TOKENS
            )

            assertEquals(
                "Il numero degli ID token non corrisponde " +
                        "al numero dei token generati.",
                result.generatedTokenCount,
                result.tokenIds.size
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
        const val MAX_GENERATED_TOKENS = 128
        const val LOG_CHUNK_SIZE = 1500

        const val OUTPUT_HEX_MARKER =
            "|OUTPUT_HEX|"

        const val TEST_PROMPT =
            "Rispondi in italiano con una sola frase " +
                    "molto breve che saluti l'utente."
    }
}