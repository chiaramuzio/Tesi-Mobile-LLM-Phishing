package com.example.phishingawareness.generation.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.di.AppContainer
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrapResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertEquals

class RuntimePromptNativeDiagnosticsIntegrationTest {

    @Test
    fun inspect_bankingRuntimePrompt_returnsNativeMetrics() {
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
            "Costruzione del prompt runtime fallita: $promptResult",
            promptResult is RuntimePromptGenerationResult.Success
        )

        promptResult as RuntimePromptGenerationResult.Success

        val artifact =
            promptResult.artifact

        println(
            "RUNTIME_PROMPT_ARTIFACT|" +
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

        assertTrue(
            "Profilo runtime inatteso: " +
                    artifact.metadata.resolvedConfigurationId,
            artifact.metadata.resolvedConfigurationId ==
                    "BANKING_MEDIUM_MEDIUM"
        )

        assertTrue(
            "Il prompt runtime è vuoto.",
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
                "Impossibile preparare modello e context: $prepareResult",
                prepareResult is LocalModelBootstrapResult.Ready
            )

            val nativeInspection =
                NativeRuntimeBridge.inspectChatPrompt(
                    prompt = artifact.text,
                    addSpecial = true
                )

            println(
                "RUNTIME_PROMPT_NATIVE|" +
                        nativeInspection
            )

            assertTrue(
                "Diagnostica nativa fallita: $nativeInspection",
                nativeInspection.startsWith(
                    "OK|CHAT_PROMPT_INSPECTION|"
                )
            )

            val promptTokenCount =
                nativeInspection
                    .substringAfter(
                        "|PROMPT_TOKEN_COUNT|"
                    )
                    .substringBefore(
                        "|BATCH_SIZE|"
                    )
                    .toIntOrNull()

            val batchSize =
                nativeInspection
                    .substringAfter(
                        "|BATCH_SIZE|"
                    )
                    .substringBefore(
                        "|DECODED_BATCH_COUNT|"
                    )
                    .toIntOrNull()

            val decodedBatchCount =
                nativeInspection
                    .substringAfter(
                        "|DECODED_BATCH_COUNT|"
                    )
                    .substringBefore(
                        "|CONTEXT_SIZE|"
                    )
                    .toIntOrNull()

            assertTrue(
                "PROMPT_TOKEN_COUNT assente o non valido: $nativeInspection",
                promptTokenCount != null &&
                        promptTokenCount > 0
            )

            assertTrue(
                "BATCH_SIZE assente o non valido: $nativeInspection",
                batchSize != null &&
                        batchSize > 0
            )

            assertTrue(
                "DECODED_BATCH_COUNT assente o non valido: $nativeInspection",
                decodedBatchCount != null &&
                        decodedBatchCount > 0
            )

            assertTrue(
                "Context operativo inatteso: $nativeInspection",
                nativeInspection.contains(
                    "|CONTEXT_SIZE|8192|"
                )
            )

            val availableOutputTokens =
                nativeInspection
                    .substringAfter(
                        "|AVAILABLE_OUTPUT_TOKENS|"
                    )
                    .substringBefore(
                        "|TEMPLATE_MS|"
                    )
                    .toIntOrNull()

            assertEquals(
                "AVAILABLE_OUTPUT_TOKENS non è coerente " +
                        "con context e prompt token.",
                CONTEXT_SIZE - requireNotNull(
                    promptTokenCount
                ),
                availableOutputTokens
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
    }
}