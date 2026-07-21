package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.prompt.PromptTemplateProvider

/**
 * Implementazione in memoria del provider dei template.
 *
 * Riceve template già definiti e indicizzati esplicitamente.
 * Non legge asset, non seleziona autonomamente un template e
 * non modifica il contenuto delle sezioni.
 */
class InMemoryPromptTemplateProvider(
    templates: List<PromptTemplate>
) : PromptTemplateProvider {

    init {
        requireTemplateIdsAreUnique(templates)
    }

    private val templatesById: Map<PromptTemplateId, PromptTemplate> =
        templates.associateBy { template ->
            template.id
        }

    override fun get(
        templateId: PromptTemplateId
    ): PromptTemplate? {
        return templatesById[templateId]
    }

    private fun requireTemplateIdsAreUnique(
        templates: List<PromptTemplate>
    ) {
        val duplicatedIds = templates
            .groupingBy { template -> template.id }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys

        require(duplicatedIds.isEmpty()) {
            "Duplicate prompt template IDs: " +
                    duplicatedIds.joinToString()
        }
    }
}