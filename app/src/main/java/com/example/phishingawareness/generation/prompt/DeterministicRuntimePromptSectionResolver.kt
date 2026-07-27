package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.ResolvedGenerationConfig
import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.model.RuntimePromptRuleSet
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionIssue
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionIssueCode
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.prompt.RuntimePromptRuleCatalog
import com.example.phishingawareness.domain.prompt.RuntimePromptSectionResolver

class DeterministicRuntimePromptSectionResolver(
    private val ruleCatalog: RuntimePromptRuleCatalog =
        FrozenRuntimePromptRuleCatalog,
    private val outputFormatOverride: String? = null
) : RuntimePromptSectionResolver {

    override fun resolve(
        parameters: ResolvedPromptParameters
    ): RuntimePromptSectionResolutionResult {
        val issues = validate(parameters)

        if (issues.isNotEmpty()) {
            return RuntimePromptSectionResolutionResult.Failure(
                issues = issues
            )
        }

        val ruleSet =
            ruleCatalog.get(parameters.scenario)
                ?: return RuntimePromptSectionResolutionResult.Failure(
                    issues = listOf(
                        RuntimePromptSectionResolutionIssue(
                            code =
                                RuntimePromptSectionResolutionIssueCode
                                    .RULE_SET_NOT_FOUND,
                            field = "scenario",
                            details = parameters.scenario.name
                        )
                    )
                )

        return RuntimePromptSectionResolutionResult.Success(
            configuration = ResolvedGenerationConfig(
                configurationId = parameters.configurationId,
                scenario = parameters.scenario,
                difficulty = parameters.difficulty,
                length = parameters.length,
                language = parameters.language,
                sections = listOf(
                    roleAndObjectiveSection(),
                    parametersSection(parameters),
                    requiredIndicatorsSection(parameters),
                    credibilityElementsSection(parameters),
                    rulesSection(
                        id = SECTION_COMMON_RULES,
                        title = "REGOLE COMUNI",
                        rules = ruleSet.commonRules
                    ),
                    rulesSection(
                        id = SECTION_SCENARIO_RULES,
                        title = "REGOLE SPECIFICHE DELLO SCENARIO",
                        rules = ruleSet.scenarioRules
                    ),
                    internalChecksSection(ruleSet),
                    outputFormatSection()
                )
            )
        )
    }

    private fun roleAndObjectiveSection(): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_ROLE_AND_OBJECTIVE,
            content = """
                Sei un generatore di simulazioni educative per un'applicazione di Cybersecurity Awareness.

                OBIETTIVO
                Genera una email di phishing simulata, credibile ma sicura, destinata esclusivamente a un esercizio formativo.
            """.trimIndent()
        )
    }

    private fun parametersSection(
        parameters: ResolvedPromptParameters
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_PARAMETERS,
            content = buildString {
                appendLine("PARAMETRI")
                appendLine(
                    "Scenario: ${parameters.scenarioLabel}"
                )
                appendLine(
                    "Difficoltà: ${parameters.difficulty.toPromptValue()}"
                )
                appendLine(
                    "Lunghezza: ${parameters.length.toPromptValue()}"
                )
                appendLine(
                    "Lingua: ${parameters.language.toLanguageLabel()}"
                )
                appendLine(
                    "Pretesto: ${parameters.pretext}"
                )
                appendLine(
                    "Identità impersonata: " +
                            parameters.impersonatedIdentity
                )
                appendLine(
                    "Marchio nominale: ${parameters.brandName}"
                )
                append(
                    "Tipo di call to action: ${parameters.ctaType}"
                )
            }
        )
    }

    private fun requiredIndicatorsSection(
        parameters: ResolvedPromptParameters
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_REQUIRED_INDICATORS,
            content = buildString {
                appendLine("INDICATORI RICHIESTI")

                parameters.requiredIndicatorPromptIds
                    .forEachIndexed { index, indicatorId ->
                        append("- ")
                        append(indicatorId)

                        if (
                            index <
                            parameters.requiredIndicatorPromptIds.lastIndex
                        ) {
                            appendLine()
                        }
                    }
            }
        )
    }

    private fun credibilityElementsSection(
        parameters: ResolvedPromptParameters
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_CREDIBILITY_ELEMENTS,
            content = buildString {
                appendLine(
                    "ELEMENTI DI CREDIBILITÀ RICHIESTI"
                )

                parameters.credibilityElements
                    .forEachIndexed { index, element ->
                        append("- ")
                        append(element)

                        if (
                            index <
                            parameters.credibilityElements.lastIndex
                        ) {
                            appendLine()
                        }
                    }
            }
        )
    }

    private fun rulesSection(
        id: String,
        title: String,
        rules: List<String>
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = id,
            content = buildString {
                appendLine(title)

                rules.forEachIndexed { index, rule ->
                    append("${index + 1}. ")
                    append(rule)

                    if (index < rules.lastIndex) {
                        appendLine()
                    }
                }
            }
        )
    }

    private fun internalChecksSection(
        ruleSet: RuntimePromptRuleSet
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_INTERNAL_CHECKS,
            content = buildString {
                appendLine(
                    "Prima di rispondere, verifica internamente che:"
                )

                ruleSet.internalChecks
                    .forEachIndexed { index, check ->
                        append("- ")
                        append(check)

                        if (
                            index <
                            ruleSet.internalChecks.lastIndex
                        ) {
                            appendLine()
                        }
                    }

                appendLine()
                append(
                    "Non mostrare questa verifica nell'output."
                )
            }
        )
    }

    private fun outputFormatSection(): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_OUTPUT_FORMAT,
            content =
                outputFormatOverride
                    ?: standardOutputFormatContent()
        )
    }

    private fun standardOutputFormatContent(): String {
        return """
        FORMATO OBBLIGATORIO
        {
          "scenario": "string",
          "difficulty": "string",
          "length": "string",
          "sender_name": "string",
          "sender_address": "string",
          "recipient": "string",
          "subject": "string",
          "body": "string",
          "pretext": "string",
          "cta_type": "string",
          "cta_text": "string",
          "present_indicators": [
            {
              "id": "string",
              "evidence": "string",
              "explanation": "string"
            }
          ],
          "credibility_elements": [
            "string"
          ],
          "educational_summary": "string"
        }
    """.trimIndent()
    }

    private fun validate(
        parameters: ResolvedPromptParameters
    ): List<RuntimePromptSectionResolutionIssue> {
        val issues =
            mutableListOf<RuntimePromptSectionResolutionIssue>()

        addMissingValueIssue(
            issues = issues,
            field = "configurationId",
            value = parameters.configurationId,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_CONFIGURATION_ID
        )

        addMissingValueIssue(
            issues = issues,
            field = "language",
            value = parameters.language,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_LANGUAGE
        )

        addMissingValueIssue(
            issues = issues,
            field = "scenarioLabel",
            value = parameters.scenarioLabel,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_SCENARIO_LABEL
        )

        addMissingValueIssue(
            issues = issues,
            field = "pretext",
            value = parameters.pretext,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_PRETEXT
        )

        addMissingValueIssue(
            issues = issues,
            field = "impersonatedIdentity",
            value = parameters.impersonatedIdentity,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_IMPERSONATED_IDENTITY
        )

        addMissingValueIssue(
            issues = issues,
            field = "brandName",
            value = parameters.brandName,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_BRAND_NAME
        )

        addMissingValueIssue(
            issues = issues,
            field = "ctaType",
            value = parameters.ctaType,
            code =
                RuntimePromptSectionResolutionIssueCode
                    .MISSING_CTA_TYPE
        )

        if (parameters.requiredIndicatorPromptIds.isEmpty()) {
            issues +=
                RuntimePromptSectionResolutionIssue(
                    code =
                        RuntimePromptSectionResolutionIssueCode
                            .EMPTY_REQUIRED_INDICATORS,
                    field = "requiredIndicatorPromptIds"
                )
        }

        if (parameters.credibilityElements.isEmpty()) {
            issues +=
                RuntimePromptSectionResolutionIssue(
                    code =
                        RuntimePromptSectionResolutionIssueCode
                            .EMPTY_CREDIBILITY_ELEMENTS,
                    field = "credibilityElements"
                )
        }

        parameters.requiredIndicatorPromptIds
            .groupingBy { indicatorId ->
                indicatorId
            }
            .eachCount()
            .filterValues { count ->
                count > 1
            }
            .keys
            .forEach { duplicatedId ->
                issues +=
                    RuntimePromptSectionResolutionIssue(
                        code =
                            RuntimePromptSectionResolutionIssueCode
                                .DUPLICATE_REQUIRED_INDICATOR,
                        field = "requiredIndicatorPromptIds",
                        details = duplicatedId
                    )
            }

        return issues
    }

    private fun addMissingValueIssue(
        issues: MutableList<RuntimePromptSectionResolutionIssue>,
        field: String,
        value: String,
        code: RuntimePromptSectionResolutionIssueCode
    ) {
        if (value.isBlank()) {
            issues +=
                RuntimePromptSectionResolutionIssue(
                    code = code,
                    field = field
                )
        }
    }

    private fun Enum<*>.toPromptValue(): String {
        return name
            .lowercase()
            .replaceFirstChar { character ->
                character.uppercase()
            }
    }

    private fun String.toLanguageLabel(): String {
        return when (lowercase()) {
            "it", "ita", "italiano" -> "Italiano"
            else -> this
        }
    }

    private companion object {

        const val SECTION_ROLE_AND_OBJECTIVE =
            "ROLE_AND_OBJECTIVE"

        const val SECTION_PARAMETERS =
            "PARAMETERS"

        const val SECTION_REQUIRED_INDICATORS =
            "REQUIRED_INDICATORS"

        const val SECTION_CREDIBILITY_ELEMENTS =
            "CREDIBILITY_ELEMENTS"

        const val SECTION_COMMON_RULES =
            "COMMON_RULES"

        const val SECTION_SCENARIO_RULES =
            "SCENARIO_RULES"

        const val SECTION_INTERNAL_CHECKS =
            "INTERNAL_CHECKS"

        const val SECTION_OUTPUT_FORMAT =
            "OUTPUT_FORMAT"
    }
}