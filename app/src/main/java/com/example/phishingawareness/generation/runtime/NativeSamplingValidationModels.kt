package com.example.phishingawareness.generation.runtime

sealed interface NativeSamplingValidationResult {

    data class Valid(
        val configuration: NativeSamplingConfiguration,
        val rawResponse: String
    ) : NativeSamplingValidationResult

    data class Invalid(
        val code: NativeSamplingValidationFailureCode,
        val rawResponse: String
    ) : NativeSamplingValidationResult
}

enum class NativeSamplingValidationFailureCode {
    INVALID_MAX_GENERATED_TOKENS,
    INVALID_TEMPERATURE,
    INVALID_TOP_K,
    INVALID_TOP_P,
    INVALID_MIN_P,
    INVALID_REPEAT_PENALTY,
    MALFORMED_NATIVE_RESPONSE
}