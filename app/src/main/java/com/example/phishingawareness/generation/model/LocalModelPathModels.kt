package com.example.phishingawareness.generation.model

sealed interface LocalModelPathResult {

    data class Available(
        val absolutePath: String,
        val sizeBytes: Long
    ) : LocalModelPathResult

    data class Unavailable(
        val code: LocalModelPathFailureCode,
        val expectedPath: String? = null,
        val details: String? = null
    ) : LocalModelPathResult
}

enum class LocalModelPathFailureCode {
    EXTERNAL_DIRECTORY_NOT_AVAILABLE,
    MODEL_FILE_NOT_FOUND,
    MODEL_PATH_NOT_A_FILE,
    MODEL_FILE_EMPTY,
    MODEL_FILE_SIZE_MISMATCH
}