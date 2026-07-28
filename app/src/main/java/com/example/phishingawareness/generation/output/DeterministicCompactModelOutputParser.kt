package com.example.phishingawareness.generation.output

import com.example.phishingawareness.domain.model.CompactModelOutputParseRequest
import com.example.phishingawareness.domain.model.CompactModelOutputParseResult
import com.example.phishingawareness.domain.model.CompactParsedPhishingEmail
import com.example.phishingawareness.domain.model.CompactParsedPhishingIndicator
import com.example.phishingawareness.domain.model.ModelOutputParseIssue
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.json.JSONException
import org.json.JSONObject

class DeterministicCompactModelOutputParser(
    private val libraryRepository: LibraryRepository
) {

    fun parse(
        request: CompactModelOutputParseRequest
    ): CompactModelOutputParseResult {
        val normalizedOutput = request.rawOutput.trim()

        if (normalizedOutput.isEmpty()) {
            return failure(
                code = ModelOutputParseIssueCode.EMPTY_OUTPUT,
                field = "rawOutput"
            )
        }

        if (
            !normalizedOutput.startsWith("{") ||
            !normalizedOutput.endsWith("}")
        ) {
            return failure(
                code = ModelOutputParseIssueCode.INVALID_JSON_BOUNDARY,
                field = "rawOutput"
            )
        }

        val root =
            try {
                JSONObject(normalizedOutput)
            } catch (exception: JSONException) {
                return failure(
                    code = ModelOutputParseIssueCode.MALFORMED_JSON,
                    field = "rawOutput",
                    details = exception.message
                )
            }

        val issues = mutableListOf<ModelOutputParseIssue>()

        val senderName =
            readRequiredString(
                root = root,
                field = "sender_name",
                issues = issues
            )

        val senderAddress =
            readRequiredString(
                root = root,
                field = "sender_address",
                issues = issues
            )

        val subject =
            readRequiredString(
                root = root,
                field = "subject",
                issues = issues
            )

        val body =
            readRequiredString(
                root = root,
                field = "body",
                issues = issues
            )

        val indicators =
            readIndicators(
                root = root,
                expectedScenario = request.expectedScenario,
                issues = issues
            )

        if (issues.isNotEmpty()) {
            return CompactModelOutputParseResult.Failure(
                issues = issues
            )
        }

        return CompactModelOutputParseResult.Success(
            email = CompactParsedPhishingEmail(
                senderName = senderName,
                senderAddress = senderAddress,
                subject = subject,
                body = body,
                presentIndicators = indicators
            )
        )
    }

    private fun readRequiredString(
        root: JSONObject,
        field: String,
        issues: MutableList<ModelOutputParseIssue>
    ): String {
        if (!root.has(field) || root.isNull(field)) {
            issues +=
                ModelOutputParseIssue(
                    code = ModelOutputParseIssueCode.MISSING_REQUIRED_FIELD,
                    field = field
                )

            return ""
        }

        val value =
            try {
                root.getString(field)
            } catch (exception: JSONException) {
                issues +=
                    ModelOutputParseIssue(
                        code = ModelOutputParseIssueCode.MISSING_REQUIRED_FIELD,
                        field = field,
                        details = exception.message
                    )

                return ""
            }

        if (value.isBlank()) {
            issues +=
                ModelOutputParseIssue(
                    code = ModelOutputParseIssueCode.EMPTY_REQUIRED_FIELD,
                    field = field
                )
        }

        return value
    }

    private fun readIndicators(
        root: JSONObject,
        expectedScenario: Scenario,
        issues: MutableList<ModelOutputParseIssue>
    ): List<CompactParsedPhishingIndicator> {
        val jsonArray =
            root.optJSONArray("present_indicators")

        if (jsonArray == null || jsonArray.length() == 0) {
            issues +=
                ModelOutputParseIssue(
                    code =
                        ModelOutputParseIssueCode
                            .INVALID_PRESENT_INDICATORS,
                    field = "present_indicators"
                )

            return emptyList()
        }

        val parsedIndicators =
            mutableListOf<CompactParsedPhishingIndicator>()

        val seenPromptIds =
            mutableSetOf<String>()

        for (index in 0 until jsonArray.length()) {
            val indicatorJson =
                jsonArray.optJSONObject(index)

            if (indicatorJson == null) {
                issues +=
                    ModelOutputParseIssue(
                        code =
                            ModelOutputParseIssueCode
                                .INVALID_PRESENT_INDICATORS,
                        field = "present_indicators[$index]"
                    )

                continue
            }

            val promptId =
                readRequiredString(
                    root = indicatorJson,
                    field = "id",
                    issues = issues
                )

            val evidence =
                readRequiredString(
                    root = indicatorJson,
                    field = "evidence",
                    issues = issues
                )

            if (promptId.isBlank()) {
                continue
            }

            if (!seenPromptIds.add(promptId)) {
                issues +=
                    ModelOutputParseIssue(
                        code =
                            ModelOutputParseIssueCode
                                .DUPLICATE_PROMPT_INDICATOR,
                        field = "present_indicators",
                        details = promptId
                    )

                continue
            }

            val definition =
                libraryRepository
                    .getIndicatorByPromptId(promptId)

            if (definition == null) {
                issues +=
                    ModelOutputParseIssue(
                        code =
                            ModelOutputParseIssueCode
                                .UNKNOWN_PROMPT_INDICATOR,
                        field = "present_indicators[$index].id",
                        details = promptId
                    )

                continue
            }

            if (
                !definition.enabled ||
                !definition.observable ||
                expectedScenario.name !in definition.scenarios
            ) {
                issues +=
                    ModelOutputParseIssue(
                        code =
                            ModelOutputParseIssueCode
                                .INDICATOR_NOT_AVAILABLE_FOR_SCENARIO,
                        field = "present_indicators[$index].id",
                        details = promptId
                    )

                continue
            }

            parsedIndicators +=
                CompactParsedPhishingIndicator(
                    promptId = promptId,
                    internalId = definition.id,
                    evidence = evidence
                )
        }

        return parsedIndicators
    }

    private fun failure(
        code: ModelOutputParseIssueCode,
        field: String? = null,
        details: String? = null
    ): CompactModelOutputParseResult.Failure {
        return CompactModelOutputParseResult.Failure(
            issues = listOf(
                ModelOutputParseIssue(
                    code = code,
                    field = field,
                    details = details
                )
            )
        )
    }
}
