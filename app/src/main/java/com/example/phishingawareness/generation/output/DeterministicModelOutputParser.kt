package com.example.phishingawareness.generation.output

import com.example.phishingawareness.domain.model.ModelOutputParseIssue
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.ModelOutputParseRequest
import com.example.phishingawareness.domain.model.ModelOutputParseResult
import com.example.phishingawareness.domain.model.ParsedPhishingEmail
import com.example.phishingawareness.domain.model.ParsedPhishingIndicator
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.modeloutput.ModelOutputParser
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DeterministicModelOutputParser(
    private val libraryRepository: LibraryRepository,
    private val jsonObjectExtractor:
    DeterministicJsonObjectExtractor =
        DeterministicJsonObjectExtractor()
) : ModelOutputParser {

    override fun parse(
        request: ModelOutputParseRequest
    ): ModelOutputParseResult {
        val normalizedOutput =
            request.rawOutput.trim()

        if (normalizedOutput.isEmpty()) {
            return failure(
                code = ModelOutputParseIssueCode.EMPTY_OUTPUT,
                field = "rawOutput"
            )
        }

        val jsonObject =
            jsonObjectExtractor.extract(
                rawOutput = normalizedOutput
            )
                ?: return failure(
                    code =
                        ModelOutputParseIssueCode
                            .INVALID_JSON_BOUNDARY,
                    field = "rawOutput"
                )

        val root =
            try {
                JSONObject(jsonObject)
            } catch (exception: JSONException) {
                return failure(
                    code =
                        ModelOutputParseIssueCode
                            .MALFORMED_JSON,
                    field = "rawOutput",
                    details = exception.message
                )
            }

        val issues = mutableListOf<ModelOutputParseIssue>()

        val scenario =
            readRequiredString(
                root = root,
                field = "scenario",
                issues = issues
            )

        val difficulty =
            readRequiredString(
                root = root,
                field = "difficulty",
                issues = issues
            )

        val length =
            readRequiredString(
                root = root,
                field = "length",
                issues = issues
            )

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

        val recipient =
            readRequiredString(
                root = root,
                field = "recipient",
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

        val pretext =
            readRequiredString(
                root = root,
                field = "pretext",
                issues = issues
            )

        val ctaType =
            readRequiredString(
                root = root,
                field = "cta_type",
                issues = issues
            )

        val ctaText =
            readRequiredString(
                root = root,
                field = "cta_text",
                issues = issues
            )

        val educationalSummary =
            readRequiredString(
                root = root,
                field = "educational_summary",
                issues = issues
            )

        val indicators =
            readIndicators(
                root = root,
                expectedScenario = request.expectedScenario,
                issues = issues
            )

        val credibilityElements =
            readStringArray(
                root = root,
                field = "credibility_elements",
                issueCode =
                    ModelOutputParseIssueCode
                        .INVALID_CREDIBILITY_ELEMENTS,
                issues = issues
            )

        if (issues.isNotEmpty()) {
            return ModelOutputParseResult.Failure(
                issues = issues
            )
        }

        return ModelOutputParseResult.Success(
            email = ParsedPhishingEmail(
                scenario = scenario,
                difficulty = difficulty,
                length = length,
                senderName = senderName,
                senderAddress = senderAddress,
                recipient = recipient,
                subject = subject,
                body = body,
                pretext = pretext,
                ctaType = ctaType,
                ctaText = ctaText,
                presentIndicators = indicators,
                credibilityElements = credibilityElements,
                educationalSummary = educationalSummary
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
                    code =
                        ModelOutputParseIssueCode
                            .MISSING_REQUIRED_FIELD,
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
                        code =
                            ModelOutputParseIssueCode
                                .MISSING_REQUIRED_FIELD,
                        field = field,
                        details = exception.message
                    )

                return ""
            }

        if (value.isBlank()) {
            issues +=
                ModelOutputParseIssue(
                    code =
                        ModelOutputParseIssueCode
                            .EMPTY_REQUIRED_FIELD,
                    field = field
                )
        }

        return value
    }

    private fun readIndicators(
        root: JSONObject,
        expectedScenario: Scenario,
        issues: MutableList<ModelOutputParseIssue>
    ): List<ParsedPhishingIndicator> {
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
            mutableListOf<ParsedPhishingIndicator>()

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

            val explanation =
                readRequiredString(
                    root = indicatorJson,
                    field = "explanation",
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
                ParsedPhishingIndicator(
                    promptId = promptId,
                    internalId = definition.id,
                    evidence = evidence,
                    explanation = explanation
                )
        }

        return parsedIndicators
    }

    private fun readStringArray(
        root: JSONObject,
        field: String,
        issueCode: ModelOutputParseIssueCode,
        issues: MutableList<ModelOutputParseIssue>
    ): List<String> {
        val jsonArray =
            root.optJSONArray(field)

        if (jsonArray == null || jsonArray.length() == 0) {
            issues +=
                ModelOutputParseIssue(
                    code = issueCode,
                    field = field
                )

            return emptyList()
        }

        return buildList {
            for (index in 0 until jsonArray.length()) {
                val value =
                    jsonArray.optString(
                        index,
                        ""
                    )

                if (value.isBlank()) {
                    issues +=
                        ModelOutputParseIssue(
                            code = issueCode,
                            field = "$field[$index]"
                        )
                } else {
                    add(value)
                }
            }
        }
    }

    private fun failure(
        code: ModelOutputParseIssueCode,
        field: String? = null,
        details: String? = null
    ): ModelOutputParseResult.Failure {
        return ModelOutputParseResult.Failure(
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