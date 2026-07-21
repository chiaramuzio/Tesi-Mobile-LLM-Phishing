package com.example.phishingawareness.domain.model

data class RuntimePromptProfile(
    val id: String,
    val scenario: Scenario,
    val difficulty: Difficulty,
    val length: ExerciseLength,
    val scenarioLabel: String,
    val pretext: String,
    val impersonatedIdentity: String,
    val brandName: String,
    val ctaType: String,
    val requiredIndicatorIds: List<String>,
    val credibilityElements: List<String>
)

data class PromptParameterResolutionRequest(
    val configurationId: String,
    val userConfiguration: UserConfiguration,
    val language: String
)

data class ResolvedPromptParameters(
    val configurationId: String,
    val profileId: String,
    val scenario: Scenario,
    val difficulty: Difficulty,
    val length: ExerciseLength,
    val language: String,
    val scenarioLabel: String,
    val pretext: String,
    val impersonatedIdentity: String,
    val brandName: String,
    val ctaType: String,
    val requiredIndicatorPromptIds: List<String>,
    val credibilityElements: List<String>
)

sealed class PromptParameterResolutionResult {

    data class Success(
        val parameters: ResolvedPromptParameters
    ) : PromptParameterResolutionResult()

    data class Failure(
        val issues: List<PromptParameterResolutionIssue>
    ) : PromptParameterResolutionResult()
}

data class PromptParameterResolutionIssue(
    val code: PromptParameterResolutionIssueCode,
    val field: String,
    val details: String? = null
)

enum class PromptParameterResolutionIssueCode {
    MISSING_CONFIGURATION_ID,
    MISSING_LANGUAGE,
    PROFILE_NOT_FOUND,
    INDICATOR_NOT_FOUND,
    INDICATOR_NOT_AVAILABLE_FOR_SCENARIO
}