package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FrozenPromptTemplateCatalogTest {

    @Test
    fun bankingReference_containsExpectedMetadata() {
        val reference = FrozenPromptTemplateCatalog.get(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertNotNull(reference)
        assertEquals(
            PromptTemplateId.BANKING_ZERO_SHOT_V12,
            reference?.id
        )
        assertEquals("12", reference?.version)
        assertEquals(Scenario.BANKING, reference?.scenario)
        assertEquals(
            "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt",
            reference?.assetPath
        )
    }

    @Test
    fun accountItReference_containsExpectedMetadata() {
        val reference = FrozenPromptTemplateCatalog.get(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3
        )

        assertNotNull(reference)
        assertEquals(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3,
            reference?.id
        )
        assertEquals("3", reference?.version)
        assertEquals(Scenario.ACCOUNT_IT, reference?.scenario)
        assertEquals(
            "prompts/zero_shot/ACCOUNT_IT_01_ZERO_SHOT_v3.txt",
            reference?.assetPath
        )
    }

    @Test
    fun catalog_containsExactlyTwoUniqueReferences() {
        val references = FrozenPromptTemplateCatalog.all

        assertEquals(2, references.size)
        assertEquals(
            references.size,
            references.map { reference -> reference.id }.distinct().size
        )
        assertEquals(
            references.size,
            references.map { reference -> reference.assetPath }.distinct().size
        )
    }

    @Test
    fun get_withUnknownCatalogEntry_returnsNull() {
        val emptyResult = FrozenPromptTemplateCatalog.all
            .firstOrNull { reference ->
                reference.assetPath == "prompts/zero_shot/not_existing.txt"
            }

        assertNull(emptyResult)
    }
}