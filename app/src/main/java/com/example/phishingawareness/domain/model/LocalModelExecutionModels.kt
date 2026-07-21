package com.example.phishingawareness.domain.model

data class LocalModelExecutionRequest(
    val prompt: String,
    val promptSha256: String,
    val seed: Int,
    val contextSize: Int,
    val maxGeneratedTokens: Int,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val minP: Float,
    val repeatPenalty: Float
)

sealed class LocalModelExecutionResult {

    data class Success(
        val rawOutput: String,
        val metadata: LocalModelExecutionMetadata
    ) : LocalModelExecutionResult()

    data class Failure(
        val code: LocalModelExecutionFailureCode,
        val details: String? = null
    ) : LocalModelExecutionResult()
}

data class LocalModelExecutionMetadata(
    val promptSha256: String,
    val seed: Int,
    val generatedCharacterCount: Int
)

enum class LocalModelExecutionFailureCode {
    MODEL_NOT_AVAILABLE,
    INVALID_REQUEST,
    EXECUTION_FAILED,
    EMPTY_OUTPUT
}