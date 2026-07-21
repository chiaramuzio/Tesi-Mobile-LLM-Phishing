package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateLoadIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult
import com.example.phishingawareness.domain.model.PromptTextReadIssueCode
import com.example.phishingawareness.domain.model.PromptTextReadResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.prompt.PromptTextSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogPromptTemplateLoaderTest {

    @Test
    fun load_bankingTemplate_returnsExpectedMetadataAndExactText() {
        val expectedText =
            "Prima riga\n\nSeconda riga con credibilità\n"

        val loader = CatalogPromptTemplateLoader(
            textSource = InMemoryPromptTextSource(
                contentsByPath = mapOf(
                    "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt" to
                            expectedText
                )
            )
        )

        val result = loader.load(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertTrue(result is PromptTemplateLoadResult.Success)

        val template =
            (result as PromptTemplateLoadResult.Success).template

        assertEquals(
            PromptTemplateId.BANKING_ZERO_SHOT_V12,
            template.id
        )
        assertEquals("12", template.version)
        assertEquals(Scenario.BANKING, template.scenario)
        assertEquals(1, template.sections.size)
        assertEquals(
            "FULL_PROMPT",
            template.sections.single().id
        )
        assertEquals(
            expectedText,
            template.sections.single().content
        )
    }

    @Test
    fun load_accountItTemplate_returnsExpectedMetadata() {
        val path =
            "prompts/zero_shot/ACCOUNT_IT_01_ZERO_SHOT_v3.txt"

        val loader = CatalogPromptTemplateLoader(
            textSource = InMemoryPromptTextSource(
                contentsByPath = mapOf(
                    path to "Prompt ACCOUNT_IT"
                )
            )
        )

        val result = loader.load(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3
        )

        assertTrue(result is PromptTemplateLoadResult.Success)

        val template =
            (result as PromptTemplateLoadResult.Success).template

        assertEquals(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3,
            template.id
        )
        assertEquals("3", template.version)
        assertEquals(Scenario.ACCOUNT_IT, template.scenario)
    }

    @Test
    fun load_whenTextCannotBeRead_returnsStructuredFailure() {
        val failingSource = object : PromptTextSource {

            override fun read(
                assetPath: String
            ): PromptTextReadResult {
                return PromptTextReadResult.Failure(
                    code = PromptTextReadIssueCode.ASSET_NOT_FOUND,
                    assetPath = assetPath
                )
            }
        }

        val loader = CatalogPromptTemplateLoader(
            textSource = failingSource
        )

        val result = loader.load(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertTrue(result is PromptTemplateLoadResult.Failure)

        val failure =
            result as PromptTemplateLoadResult.Failure

        assertEquals(
            PromptTemplateLoadIssueCode.TEXT_READ_FAILURE,
            failure.code
        )
        assertEquals(
            PromptTemplateId.BANKING_ZERO_SHOT_V12,
            failure.templateId
        )
        assertTrue(
            failure.details?.contains(
                "ASSET_NOT_FOUND"
            ) == true
        )
    }
}