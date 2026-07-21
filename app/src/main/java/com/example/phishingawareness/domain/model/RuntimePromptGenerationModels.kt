package com.example.phishingawareness.domain.model

data class RuntimePromptGenerationRequest(
    val configurationId: String,
    val userConfiguration: UserConfiguration,
    val language: String,
    val buildContext: PromptBuildContext
)

sealed class RuntimePromptGenerationResult {

    data class Success(
        val artifact: PromptArtifact
    ) : RuntimePromptGenerationResult()

    data class Failure(
        val stage: RuntimePromptGenerationFailureStage,
        val details: String
    ) : RuntimePromptGenerationResult()
}

enum class RuntimePromptGenerationFailureStage {
    REQUEST_MAPPING,
    PARAMETER_RESOLUTION,
    SECTION_RESOLUTION,
    PROMPT_BUILDING
}