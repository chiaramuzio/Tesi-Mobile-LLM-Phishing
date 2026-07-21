package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult
import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class PromptGenerationPipelineTest {

    @Test
    fun bankingPipeline_preservesExactTextAndProducesExpectedHash() {
        val assetPath =
            "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt"

        val originalText =
            "  Prima riga BANKING  \r\n\r\n" +
                    "Seconda riga con credibilità\n"

        val textSource = InMemoryPromptTextSource(
            contentsByPath = mapOf(
                assetPath to originalText
            )
        )

        val loader = CatalogPromptTemplateLoader(
            textSource = textSource
        )

        val loadResult = loader.load(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertTrue(
            loadResult is PromptTemplateLoadResult.Success
        )

        val template =
            (loadResult as PromptTemplateLoadResult.Success)
                .template

        val resolver =
            DeterministicPromptTemplateResolver()

        val resolutionResult = resolver.resolve(
            PromptTemplateResolutionRequest(
                configurationId =
                    "BANKING_ZERO_SHOT_V12_MEDIUM_MEDIUM_IT",
                template = template,
                difficulty = Difficulty.MEDIUM,
                length = ExerciseLength.MEDIUM,
                language = "it"
            )
        )

        assertTrue(
            resolutionResult
                    is PromptTemplateResolutionResult.Success
        )

        val configuration =
            (
                    resolutionResult
                            as PromptTemplateResolutionResult.Success
                    )
                .configuration

        val builder =
            DeterministicPromptBuilder()

        val buildResult = builder.build(
            configuration = configuration,
            context = PromptBuildContext(
                builderVersion = "1",
                templateId =
                    PromptTemplateId
                        .BANKING_ZERO_SHOT_V12
                        .name,
                templateVersion = "12",
                libraryId = "phishing-awareness-library",
                libraryVersion = "1",
                librarySchemaVersion = 1
            )
        )

        assertTrue(
            buildResult is PromptBuildResult.Success
        )

        val artifact =
            (buildResult as PromptBuildResult.Success)
                .artifact

        assertEquals(
            originalText,
            artifact.text
        )

        assertEquals(
            sha256(originalText),
            artifact.metadata.promptSha256
        )

        assertEquals(
            "BANKING_ZERO_SHOT_V12_MEDIUM_MEDIUM_IT",
            artifact.metadata.resolvedConfigurationId
        )

        assertEquals(
            PromptTemplateId.BANKING_ZERO_SHOT_V12.name,
            artifact.metadata.buildContext.templateId
        )
    }

    @Test
    fun accountItPipeline_preservesMetadataAndExactText() {
        val assetPath =
            "prompts/zero_shot/ACCOUNT_IT_01_ZERO_SHOT_v3.txt"

        val originalText =
            "Prompt ACCOUNT_IT\n\n" +
                    "Testo con accenti: credenziali e identità\n"

        val loader = CatalogPromptTemplateLoader(
            textSource = InMemoryPromptTextSource(
                contentsByPath = mapOf(
                    assetPath to originalText
                )
            )
        )

        val loadResult = loader.load(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3
        )

        assertTrue(
            loadResult is PromptTemplateLoadResult.Success
        )

        val template =
            (loadResult as PromptTemplateLoadResult.Success)
                .template

        val resolver =
            DeterministicPromptTemplateResolver()

        val resolutionResult = resolver.resolve(
            PromptTemplateResolutionRequest(
                configurationId =
                    "ACCOUNT_IT_ZERO_SHOT_V3_MEDIUM_MEDIUM_IT",
                template = template,
                difficulty = Difficulty.MEDIUM,
                length = ExerciseLength.MEDIUM,
                language = "it"
            )
        )

        assertTrue(
            resolutionResult
                    is PromptTemplateResolutionResult.Success
        )

        val configuration =
            (
                    resolutionResult
                            as PromptTemplateResolutionResult.Success
                    )
                .configuration

        val builder =
            DeterministicPromptBuilder()

        val buildResult = builder.build(
            configuration = configuration,
            context = PromptBuildContext(
                builderVersion = "1",
                templateId =
                    PromptTemplateId
                        .ACCOUNT_IT_ZERO_SHOT_V3
                        .name,
                templateVersion = "3",
                libraryId = "phishing-awareness-library",
                libraryVersion = "1",
                librarySchemaVersion = 1
            )
        )

        assertTrue(
            buildResult is PromptBuildResult.Success
        )

        val artifact =
            (buildResult as PromptBuildResult.Success)
                .artifact

        assertEquals(
            originalText,
            artifact.text
        )

        assertEquals(
            sha256(originalText),
            artifact.metadata.promptSha256
        )

        assertEquals(
            "ACCOUNT_IT_ZERO_SHOT_V3_MEDIUM_MEDIUM_IT",
            artifact.metadata.resolvedConfigurationId
        )

        assertEquals(
            "3",
            artifact.metadata.buildContext.templateVersion
        )
    }

    private fun sha256(
        text: String
    ): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte ->
                "%02x".format(byte)
            }
    }
}