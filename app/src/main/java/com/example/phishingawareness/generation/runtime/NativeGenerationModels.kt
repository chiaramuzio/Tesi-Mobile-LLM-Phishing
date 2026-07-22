package com.example.phishingawareness.generation.runtime

data class NativeGenerationRequest(
    val prompt: String,
    val addSpecial: Boolean,
    val sampling:
    NativeSamplingConfiguration
)

sealed interface NativeGenerationResult {

    data class Success(
        val requestedTokenCount: Int,
        val generatedTokenCount: Int,
        val reachedEndOfGeneration: Boolean,
        val tokenIds: List<Int>,
        val outputBytes: ByteArray
    ) : NativeGenerationResult {

        init {
            require(requestedTokenCount > 0)
            require(generatedTokenCount > 0)
            require(generatedTokenCount <= requestedTokenCount)
            require(tokenIds.size == generatedTokenCount)
        }
    }

    data class Failure(
        val code: NativeGenerationFailureCode,
        val rawResponse: String
    ) : NativeGenerationResult
}

enum class NativeGenerationFailureCode {
    PROMPT_NULL,
    PROMPT_EMPTY,
    INVALID_MAX_GENERATED_TOKENS,

    INVALID_SAMPLING_CONFIGURATION,
    MODEL_NOT_LOADED,
    CONTEXT_NOT_CREATED,
    TOKENIZATION_FAILED,
    CONTEXT_SIZE_EXCEEDED,
    PROMPT_DECODE_FAILED,
    SAMPLER_CREATION_FAILED,
    TOKEN_PIECE_FAILED,
    GENERATED_TOKEN_DECODE_FAILED,
    MALFORMED_NATIVE_RESPONSE,
    UNKNOWN_NATIVE_ERROR
}