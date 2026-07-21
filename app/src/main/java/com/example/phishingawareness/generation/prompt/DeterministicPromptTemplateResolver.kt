package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplateResolutionIssue
import com.example.phishingawareness.domain.model.PromptTemplateResolutionIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult
import com.example.phishingawareness.domain.model.ResolvedGenerationConfig
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.prompt.PromptTemplateResolver

/**
 * Risolve deterministicamente un template operativo.
 *
 * Mantiene invariati ordine e contenuto delle sezioni.
 * Non applica trim, sostituzioni o normalizzazioni.
 */
class DeterministicPromptTemplateResolver : PromptTemplateResolver {

    override fun resolve(
        request: PromptTemplateResolutionRequest
    ): PromptTemplateResolutionResult {
        val issues = validate(request)

        if (issues.isNotEmpty()) {
            return PromptTemplateResolutionResult.Failure(
                issues = issues
            )
        }

        return PromptTemplateResolutionResult.Success(
            configuration = ResolvedGenerationConfig(
                configurationId = request.configurationId,
                scenario = request.template.scenario,
                difficulty = request.difficulty,
                length = request.length,
                language = request.language,
                sections = request.template.sections.map { section ->
                    ResolvedPromptSection(
                        id = section.id,
                        content = section.content
                    )
                }
            )
        )
    }

    private fun validate(
        request: PromptTemplateResolutionRequest
    ): List<PromptTemplateResolutionIssue> {
        val issues = mutableListOf<PromptTemplateResolutionIssue>()

        if (request.configurationId.isBlank()) {
            issues += PromptTemplateResolutionIssue(
                code = PromptTemplateResolutionIssueCode
                    .MISSING_CONFIGURATION_ID,
                field = "configurationId"
            )
        }

        if (request.language.isBlank()) {
            issues += PromptTemplateResolutionIssue(
                code = PromptTemplateResolutionIssueCode
                    .MISSING_LANGUAGE,
                field = "language"
            )
        }

        if (request.template.version.isBlank()) {
            issues += PromptTemplateResolutionIssue(
                code = PromptTemplateResolutionIssueCode
                    .MISSING_TEMPLATE_VERSION,
                field = "template.version"
            )
        }

        if (request.template.sections.isEmpty()) {
            issues += PromptTemplateResolutionIssue(
                code = PromptTemplateResolutionIssueCode
                    .EMPTY_TEMPLATE_SECTIONS,
                field = "template.sections"
            )
        }

        request.template.sections.forEachIndexed { index, section ->
            if (section.id.isBlank()) {
                issues += PromptTemplateResolutionIssue(
                    code = PromptTemplateResolutionIssueCode
                        .EMPTY_SECTION_ID,
                    field = "template.sections[$index].id"
                )
            }

            if (section.content.isBlank()) {
                issues += PromptTemplateResolutionIssue(
                    code = PromptTemplateResolutionIssueCode
                        .EMPTY_SECTION_CONTENT,
                    field = "template.sections[$index].content"
                )
            }
        }

        val duplicatedIds = request.template.sections
            .filter { section -> section.id.isNotBlank() }
            .groupingBy { section -> section.id }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys

        duplicatedIds.forEach { duplicatedId ->
            issues += PromptTemplateResolutionIssue(
                code = PromptTemplateResolutionIssueCode
                    .DUPLICATE_SECTION_ID,
                field = "template.sections",
                details = duplicatedId
            )
        }

        return issues
    }
}