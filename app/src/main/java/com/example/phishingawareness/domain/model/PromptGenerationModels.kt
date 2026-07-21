package com.example.phishingawareness.domain.model

/**
 * Richiesta completa necessaria per generare un prompt operativo.
 */
data class PromptGenerationRequest(
    val configurationId: String,
    val templateId: PromptTemplateId,
    val difficulty: Difficulty,
    val length: ExerciseLength,
    val language: String,
    val buildContext: PromptBuildContext
)

/**
 * Risultato complessivo della pipeline di generazione del prompt.
 */
sealed interface PromptGenerationResult {

    data class Success(
        val artifact: PromptArtifact
    ) : PromptGenerationResult

    data class Failure(
        val stage: PromptGenerationFailureStage,
        val details: String? = null
    ) : PromptGenerationResult
}

/**
 * Identifica la fase della pipeline in cui si è verificato il problema.
 */
enum class PromptGenerationFailureStage {
    TEMPLATE_LOADING,
    TEMPLATE_RESOLUTION,
    PROMPT_BUILDING
}