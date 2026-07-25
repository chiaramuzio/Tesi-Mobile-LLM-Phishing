package com.example.phishingawareness.generation.model

import com.example.phishingawareness.generation.runtime.LocalModelSessionResult

sealed interface LocalModelBootstrapResult {

    data class Ready(
        val modelPath: String,
        val modelSizeBytes: Long,
        val session: LocalModelSessionResult.Ready
    ) : LocalModelBootstrapResult

    data class PathFailure(
        val failure: LocalModelPathResult.Unavailable
    ) : LocalModelBootstrapResult

    data class SessionFailure(
        val failure: LocalModelSessionResult.Failure
    ) : LocalModelBootstrapResult
}