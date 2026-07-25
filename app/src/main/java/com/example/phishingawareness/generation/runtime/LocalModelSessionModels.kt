package com.example.phishingawareness.generation.runtime

sealed interface LocalModelSessionResult {

    data class Ready(
        val modelAlreadyLoaded: Boolean,
        val contextAlreadyReady: Boolean,
        val contextSize: Int
    ) : LocalModelSessionResult

    data object Released :
        LocalModelSessionResult

    data class Failure(
        val stage: LocalModelSessionFailureStage,
        val code: LocalModelSessionFailureCode,
        val details: String? = null
    ) : LocalModelSessionResult
}

enum class LocalModelSessionFailureStage {
    REQUEST_VALIDATION,
    MODEL_LOADING,
    CONTEXT_CREATION,
    CONTEXT_RELEASE,
    MODEL_RELEASE
}

enum class LocalModelSessionFailureCode {
    MODEL_PATH_EMPTY,
    INVALID_CONTEXT_SIZE,
    MODEL_LOAD_FAILED,
    CONTEXT_CREATE_FAILED,
    CONTEXT_SIZE_MISMATCH,
    CONTEXT_RELEASE_FAILED,
    MODEL_RELEASE_FAILED,
    UNKNOWN_NATIVE_RESPONSE
}