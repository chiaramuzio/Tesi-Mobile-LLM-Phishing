package com.example.phishingawareness.domain.model

data class CompactModelOutputParseRequest(
    val rawOutput: String,
    val expectedScenario: Scenario
)

data class CompactParsedPhishingEmail(
    val senderName: String,
    val senderAddress: String,
    val subject: String,
    val body: String,
    val presentIndicators: List<CompactParsedPhishingIndicator>
)

data class CompactParsedPhishingIndicator(
    val promptId: String,
    val internalId: String,
    val evidence: String
)

sealed class CompactModelOutputParseResult {

    data class Success(
        val email: CompactParsedPhishingEmail
    ) : CompactModelOutputParseResult()

    data class Failure(
        val issues: List<ModelOutputParseIssue>
    ) : CompactModelOutputParseResult()
}
