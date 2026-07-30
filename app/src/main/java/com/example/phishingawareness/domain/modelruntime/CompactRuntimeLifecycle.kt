package com.example.phishingawareness.domain.modelruntime

interface CompactRuntimeLifecycle {

    fun prepare(
        contextSize: Int
    ): CompactRuntimePreparationResult

    fun release()
}

sealed class CompactRuntimePreparationResult {

    object Ready :
        CompactRuntimePreparationResult()

    data class Failure(
        val details: String
    ) : CompactRuntimePreparationResult()
}