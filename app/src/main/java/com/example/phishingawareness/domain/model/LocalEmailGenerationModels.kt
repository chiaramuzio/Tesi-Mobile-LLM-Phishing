package com.example.phishingawareness.domain.model

data class LocalEmailGenerationOptions(
    val seed: Int = 101,
    val contextSize: Int = 8192,
    val maxGeneratedTokens: Int = 1200,
    val temperature: Float = 0.4f,
    val topK: Int = 40,
    val topP: Float = 0.90f,
    val minP: Float = 0.05f,
    val repeatPenalty: Float = 1.05f
)

sealed class LocalEmailGenerationResult {

    data class Success(
        val email: GeneratedEmail,
        val promptMetadata: PromptMetadata,
        val executionMetadata: LocalModelExecutionMetadata
    ) : LocalEmailGenerationResult()

    data class Failure(
        val stage: LocalEmailGenerationFailureStage,
        val details: String
    ) : LocalEmailGenerationResult()
}

enum class LocalEmailGenerationFailureStage {
    REQUEST_MAPPING,
    PROMPT_BUILDING,
    MODEL_EXECUTION,
    OUTPUT_PARSING,
    EMAIL_MAPPING
}