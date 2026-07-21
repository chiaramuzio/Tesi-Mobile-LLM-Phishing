package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateLoadIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult
import com.example.phishingawareness.domain.model.PromptTemplateSection
import com.example.phishingawareness.domain.model.PromptTextReadResult
import com.example.phishingawareness.domain.prompt.PromptTemplateCatalog
import com.example.phishingawareness.domain.prompt.PromptTemplateLoader
import com.example.phishingawareness.domain.prompt.PromptTextSource


/**
 * Carica i template descritti da un catalogo.
 *
 * Il testo letto dalla sorgente viene conservato integralmente in una sola
 * sezione, senza trim, sostituzioni o normalizzazioni.
 */
class CatalogPromptTemplateLoader(
    private val catalog: PromptTemplateCatalog,
    private val textSource: PromptTextSource
) : PromptTemplateLoader {

    override fun load(
        templateId: PromptTemplateId
    ): PromptTemplateLoadResult {
        val reference = catalog.get(templateId)
            ?: return PromptTemplateLoadResult.Failure(
                code = PromptTemplateLoadIssueCode.TEMPLATE_NOT_FOUND,
                templateId = templateId
            )

        return when (
            val textResult = textSource.read(reference.assetPath)
        ) {
            is PromptTextReadResult.Success -> {
                PromptTemplateLoadResult.Success(
                    template = PromptTemplate(
                        id = reference.id,
                        version = reference.version,
                        scenario = reference.scenario,
                        sections = listOf(
                            PromptTemplateSection(
                                id = FULL_PROMPT_SECTION_ID,
                                content = textResult.text
                            )
                        )
                    )
                )
            }

            is PromptTextReadResult.Failure -> {
                PromptTemplateLoadResult.Failure(
                    code = PromptTemplateLoadIssueCode.TEXT_READ_FAILURE,
                    templateId = templateId,
                    details = buildFailureDetails(textResult)
                )
            }
        }
    }

    private fun buildFailureDetails(
        failure: PromptTextReadResult.Failure
    ): String {
        return listOfNotNull(
            failure.code.name,
            failure.assetPath,
            failure.details
        ).joinToString(separator = ": ")
    }

    private companion object {
        const val FULL_PROMPT_SECTION_ID = "FULL_PROMPT"
    }

}