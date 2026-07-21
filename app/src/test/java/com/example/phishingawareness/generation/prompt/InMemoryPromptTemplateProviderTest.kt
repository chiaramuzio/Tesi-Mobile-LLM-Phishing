package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateSection
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class InMemoryPromptTemplateProviderTest {

    @Test
    fun get_withAvailableId_returnsCorrespondingTemplate() {
        val bankingTemplate = bankingTemplate()
        val accountItTemplate = accountItTemplate()

        val provider = InMemoryPromptTemplateProvider(
            templates = listOf(
                bankingTemplate,
                accountItTemplate
            )
        )

        val result = provider.get(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertEquals(bankingTemplate, result)
    }

    @Test
    fun get_withUnavailableId_returnsNull() {
        val provider = InMemoryPromptTemplateProvider(
            templates = listOf(
                bankingTemplate()
            )
        )

        val result = provider.get(
            PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3
        )

        assertNull(result)
    }

    @Test
    fun constructor_withDuplicateIds_throwsException() {
        val firstTemplate = bankingTemplate()

        val secondTemplate = bankingTemplate().copy(
            version = "duplicate",
            sections = listOf(
                PromptTemplateSection(
                    id = "DUPLICATE",
                    content = "Contenuto duplicato"
                )
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            InMemoryPromptTemplateProvider(
                templates = listOf(
                    firstTemplate,
                    secondTemplate
                )
            )
        }
    }

    @Test
    fun get_preservesTemplateSectionOrder() {
        val template = bankingTemplate()

        val provider = InMemoryPromptTemplateProvider(
            templates = listOf(template)
        )

        val result = provider.get(
            PromptTemplateId.BANKING_ZERO_SHOT_V12
        )

        assertEquals(
            listOf("ROLE", "OBJECTIVE"),
            result?.sections?.map { section -> section.id }
        )
    }

    private fun bankingTemplate(): PromptTemplate {
        return PromptTemplate(
            id = PromptTemplateId.BANKING_ZERO_SHOT_V12,
            version = "12",
            scenario = Scenario.BANKING,
            sections = listOf(
                PromptTemplateSection(
                    id = "ROLE",
                    content = "Ruolo del modello"
                ),
                PromptTemplateSection(
                    id = "OBJECTIVE",
                    content = "Obiettivo formativo"
                )
            )
        )
    }

    private fun accountItTemplate(): PromptTemplate {
        return PromptTemplate(
            id = PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3,
            version = "3",
            scenario = Scenario.ACCOUNT_IT,
            sections = listOf(
                PromptTemplateSection(
                    id = "ROLE",
                    content = "Ruolo del modello"
                ),
                PromptTemplateSection(
                    id = "OBJECTIVE",
                    content = "Obiettivo formativo"
                )
            )
        )
    }
}