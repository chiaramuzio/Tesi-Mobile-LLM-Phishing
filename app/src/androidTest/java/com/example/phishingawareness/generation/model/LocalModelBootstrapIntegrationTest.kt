package com.example.phishingawareness.generation.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.generation.runtime.DeterministicLocalModelSession
import com.example.phishingawareness.generation.runtime.LocalModelSessionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelBootstrapIntegrationTest {

    @Test
    fun prepare_realModel_createsAndReleasesNativeSession() {
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
                    contextSize =
                        TEST_CONTEXT_SIZE
                )

            assertTrue(
                buildString {
                    append(
                        "Preparazione del modello non riuscita. "
                    )

                    when (prepareResult) {
                        is LocalModelBootstrapResult.PathFailure -> {
                            append("Errore percorso: ")
                            append(prepareResult.failure.code)
                            append("; percorso: ")
                            append(
                                prepareResult
                                    .failure
                                    .expectedPath
                            )
                            append("; dettagli: ")
                            append(
                                prepareResult
                                    .failure
                                    .details
                            )
                        }

                        is LocalModelBootstrapResult.SessionFailure -> {
                            append("Errore sessione: ")
                            append(
                                prepareResult
                                    .failure
                                    .stage
                            )
                            append("/")
                            append(
                                prepareResult
                                    .failure
                                    .code
                            )
                            append("; dettagli: ")
                            append(
                                prepareResult
                                    .failure
                                    .details
                            )
                        }

                        is LocalModelBootstrapResult.Ready -> {
                            append("Nessun errore.")
                        }
                    }
                },
                prepareResult is
                        LocalModelBootstrapResult.Ready
            )

            prepareResult as
                    LocalModelBootstrapResult.Ready

            assertEquals(
                TEST_CONTEXT_SIZE,
                prepareResult.session.contextSize
            )

            assertEquals(
                EXPECTED_MODEL_SIZE_BYTES,
                prepareResult.modelSizeBytes
            )

            assertTrue(
                bootstrap.isReady()
            )
        } finally {
            val releaseResult =
                bootstrap.release()

            assertEquals(
                LocalModelSessionResult.Released,
                releaseResult
            )

            assertFalse(
                bootstrap.isReady()
            )
        }
    }

    private companion object {
        /*
         * Primo checkpoint tecnico volutamente ridotto.
         * Dopo il successo proveremo il context operativo 8192.
         */
        const val TEST_CONTEXT_SIZE =
            2048

        const val EXPECTED_MODEL_SIZE_BYTES =
            1_003_541_152L
    }
}