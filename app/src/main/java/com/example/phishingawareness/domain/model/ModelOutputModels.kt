package com.example.phishingawareness.domain.model

data class ModelOutputParseRequest(
    val rawOutput: String,
    val expectedScenario: Scenario
)

data class ParsedPhishingEmail(
    val scenario: String,
    val difficulty: String,
    val length: String,
    val senderName: String,
    val senderAddress: String,
    val recipient: String,
    val subject: String,
    val body: String,
    val pretext: String,
    val ctaType: String,
    val ctaText: String,
    val presentIndicators: List<ParsedPhishingIndicator>,
    val credibilityElements: List<String>,
    val educationalSummary: String
)

data class ParsedPhishingIndicator(
    val promptId: String,
    val internalId: String,
    val evidence: String,
    val explanation: String
)

sealed class ModelOutputParseResult {

    data class Success(
        val email: ParsedPhishingEmail
    ) : ModelOutputParseResult()

    data class Failure(
        val issues: List<ModelOutputParseIssue>
    ) : ModelOutputParseResult()
}

data class ModelOutputParseIssue(
    val code: ModelOutputParseIssueCode,
    val field: String? = null,
    val details: String? = null
)

enum class ModelOutputParseIssueCode {
    EMPTY_OUTPUT,
    INVALID_JSON_BOUNDARY,
    MALFORMED_JSON,
    MISSING_REQUIRED_FIELD,
    EMPTY_REQUIRED_FIELD,
    INVALID_PRESENT_INDICATORS,
    INVALID_CREDIBILITY_ELEMENTS,
    UNKNOWN_PROMPT_INDICATOR,
    INDICATOR_NOT_AVAILABLE_FOR_SCENARIO,
    DUPLICATE_PROMPT_INDICATOR
}