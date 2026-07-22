package com.example.phishingawareness.generation.runtime

fun interface NativeSamplingConfigurationValidator {

    fun validate(
        configuration: NativeSamplingConfiguration
    ): NativeSamplingValidationResult
}