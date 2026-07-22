package com.example.phishingawareness.generation.runtime

fun interface NativeGenerationExecutor {

    fun generate(
        request: NativeGenerationRequest
    ): NativeGenerationResult
}