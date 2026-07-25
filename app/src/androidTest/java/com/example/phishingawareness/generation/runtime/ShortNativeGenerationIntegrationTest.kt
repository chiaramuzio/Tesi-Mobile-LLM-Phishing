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

            val runtime =
                NativeLocalGenerationRuntime()

            val startedAt =
                SystemClock.elapsedRealtime()

            val result =
                runtime.generate(
                    prompt =
                        "Rispondi in italiano con una sola frase " +
                                "molto breve che saluti l'utente.",
                    parameters =
                        LocalGenerationParameters(
                            maxGeneratedTokens =
                                MAX_GENERATED_TOKENS,
                            temperature = 0.4f,
                            topK = 40,
                            topP = 0.90f,
                            minP = 0.05f,
                            repeatPenalty = 1.05f,
                            seed = 101
                        )
                )

            val elapsedMilliseconds =
                SystemClock.elapsedRealtime() -
                        startedAt

            println(
                "SHORT_NATIVE_RESULT|" +
                        "elapsedMs=$elapsedMilliseconds|" +
                        "generatedTokens=${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawText=${result.rawText}"
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
                        "${result.generatedTokenCount} > $MAX_GENERATED_TOKENS.",
                result.generatedTokenCount <=
                        MAX_GENERATED_TOKENS
            )

            assertEquals(
                "Il numero degli ID token non corrisponde " +
                        "al numero dei token generati.",
                result.generatedTokenCount,
                result.tokenIds.size
            )

            println(
                "SHORT_GENERATION_RESULT|" +
                        "elapsedMs=$elapsedMilliseconds|" +
                        "generatedTokens=" +
                        "${result.generatedTokenCount}|" +
                        "eog=${result.reachedEndOfGeneration}|" +
                        "rawText=${result.rawText}"
            )
        } finally {
            bootstrap.release()

            assertFalse(
                bootstrap.isReady()
            )
        }
    }

    private companion object {
        const val CONTEXT_SIZE = 8192
        const val MAX_GENERATED_TOKENS = 128
    }
}