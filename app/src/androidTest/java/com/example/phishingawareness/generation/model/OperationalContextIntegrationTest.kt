package com.example.phishingawareness.generation.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.generation.runtime.DeterministicLocalModelSession
import com.example.phishingawareness.generation.runtime.LocalModelSessionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationalContextIntegrationTest {

    @Test
    fun prepare_realModelWithOperationalContext_returnsReady() {
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
            val result =
                bootstrap.prepare(
                    contextSize = OPERATIONAL_CONTEXT_SIZE
                )

            assertTrue(
                buildString {
                    append(
                        "Preparazione del context operativo fallita. "
                    )

                    when (result) {
                        is LocalModelBootstrapResult.PathFailure -> {
                            append("PATH/")
                            append(result.failure.code)
                            append(": ")
                            append(result.failure.details)
                        }

                        is LocalModelBootstrapResult.SessionFailure -> {
                            append(result.failure.stage)
                            append("/")
                            append(result.failure.code)
                            append(": ")
                            append(result.failure.details)
                        }

                        is LocalModelBootstrapResult.Ready -> {
                            append("Nessun errore.")
                        }
                    }
                },
                result is LocalModelBootstrapResult.Ready
            )

            result as LocalModelBootstrapResult.Ready

            assertEquals(
                OPERATIONAL_CONTEXT_SIZE,
                result.session.contextSize
            )

            assertTrue(
                bootstrap.isReady()
            )
        } finally {
            assertEquals(
                LocalModelSessionResult.Released,
                bootstrap.release()
            )

            assertFalse(
                bootstrap.isReady()
            )
        }
    }

    private companion object {
        const val OPERATIONAL_CONTEXT_SIZE = 8192
    }
}