package com.example.phishingawareness.generation.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidLocalModelPathProviderTest {

    @Test
    fun resolve_modelInstalledOnDevice_returnsAvailable() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        val provider =
            AndroidLocalModelPathProvider(
                context = context
            )

        val result =
            provider.resolve()

        assertTrue(
            buildString {
                append("Il modello non Ã¨ disponibile sul dispositivo. ")

                if (result is LocalModelPathResult.Unavailable) {
                    append("Codice: ")
                    append(result.code.name)
                    append("; percorso atteso: ")
                    append(result.expectedPath)
                    append("; dettagli: ")
                    append(result.details)
                }
            },
            result is LocalModelPathResult.Available
        )

        result as LocalModelPathResult.Available

        assertTrue(
            result.absolutePath.endsWith(
                "/files/models/" +
                        "gemma-3-1b-it-q4_0.gguf"
            )
        )

        assertEquals(
            1_003_541_152L,
            result.sizeBytes
        )
    }
}
