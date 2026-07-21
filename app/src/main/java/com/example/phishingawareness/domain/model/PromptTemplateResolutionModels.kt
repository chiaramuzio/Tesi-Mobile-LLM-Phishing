package com.example.phishingawareness.domain.model

/**
 * Dati necessari per trasformare un template caricato
 * in una configurazione pronta per il PromptBuilder.
 */
data class PromptTemplateResolutionRequest(
    val configurationId: String,
    val template: PromptTemplate,
    val difficulty: Difficulty,
    val length: ExerciseLength,
    val language: String
)

/**
 * Risultato della risoluzione di un template.
 */
sealed interface PromptTemplateResolutionResult {

    data class Success(
        val configuration: ResolvedGenerationConfig
    ) : PromptTemplateResolutionResult

    data class Failure(
        val issues: List<PromptTemplateResolutionIssue>
    ) : PromptTemplateResolutionResult
}

/**
 * Problema rilevato durante la risoluzione del template.
 */
data class PromptTemplateResolutionIssue(
    val code: PromptTemplateResolutionIssueCode,
    val field: String? = null,
    val details: String? = null
)

enum class PromptTemplateResolutionIssueCode {
    MISSING_CONFIGURATION_ID,
    MISSING_LANGUAGE,
    MISSING_TEMPLATE_VERSION,
    EMPTY_TEMPLATE_SECTIONS,
    EMPTY_SECTION_ID,
    EMPTY_SECTION_CONTENT,
    DUPLICATE_SECTION_ID
}