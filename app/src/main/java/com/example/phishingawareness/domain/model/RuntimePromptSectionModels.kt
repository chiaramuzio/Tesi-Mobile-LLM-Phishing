package com.example.phishingawareness.domain.model

sealed class RuntimePromptSectionResolutionResult {

    data class Success(
        val configuration: ResolvedGenerationConfig
    ) : RuntimePromptSectionResolutionResult()

    data class Failure(
        val issues: List<RuntimePromptSectionResolutionIssue>
    ) : RuntimePromptSectionResolutionResult()
}

data class RuntimePromptSectionResolutionIssue(
    val code: RuntimePromptSectionResolutionIssueCode,
    val field: String,
    val details: String? = null
)

enum class RuntimePromptSectionResolutionIssueCode {
    MISSING_CONFIGURATION_ID,
    MISSING_LANGUAGE,
    MISSING_SCENARIO_LABEL,
    MISSING_PRETEXT,
    MISSING_IMPERSONATED_IDENTITY,
    MISSING_BRAND_NAME,
    MISSING_CTA_TYPE,
    EMPTY_REQUIRED_INDICATORS,
    EMPTY_CREDIBILITY_ELEMENTS,
    DUPLICATE_REQUIRED_INDICATOR,
    RULE_SET_NOT_FOUND
}