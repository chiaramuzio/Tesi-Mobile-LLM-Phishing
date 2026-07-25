package com.example.phishingawareness.generation.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrapResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeCpuCapabilitiesIntegrationTest {

    @Test
    fun inspect_nativeBackendAndOperationalContext_returnsDiagnostics() {
        val systemInfo =
            NativeRuntimeBridge.systemInfo()

        println(
            "NATIVE_SYSTEM_INFO|" +
                    escapeForLog(
                        systemInfo
                    )
        )

        assertTrue(
            "Informazioni native non disponibili: $systemInfo",
            systemInfo.startsWith(
                "OK|SYSTEM_INFO|"
            )
        )

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

            val contextRuntimeInfo =
                NativeRuntimeBridge.contextRuntimeInfo()

            println(
                "NATIVE_CONTEXT_RUNTIME_INFO|" +
                        contextRuntimeInfo
            )

            assertTrue(
                "Informazioni del context non disponibili: " +
                        contextRuntimeInfo,
                contextRuntimeInfo.startsWith(
                    "OK|CONTEXT_RUNTIME_INFO|"
                )
            )

            assertTrue(
                "Il context operativo non è 8192: $contextRuntimeInfo",
                contextRuntimeInfo.contains(
                    "|CONTEXT_SIZE|8192|"
                )
            )

            assertTrue(
                "I thread di generazione effettivi non sono 4: " +
                        contextRuntimeInfo,
                contextRuntimeInfo.contains(
                    "|GENERATION_THREADS|4|"
                )
            )

            assertTrue(
                "I thread batch effettivi non sono 4: " +
                        contextRuntimeInfo,
                contextRuntimeInfo.contains(
                    "|BATCH_THREADS|4|"
                )
            )
        } finally {
            bootstrap.release()

            assertFalse(
                bootstrap.isReady()
            )
        }
    }

    private fun escapeForLog(
        value: String
    ): String {
        return value
            .replace(
                "\\",
                "\\\\"
            )
            .replace(
                "\r",
                "\\r"
            )
            .replace(
                "\n",
                "\\n"
            )
            .replace(
                "\t",
                "\\t"
            )
    }

    private companion object {
        const val CONTEXT_SIZE = 8192
    }
}