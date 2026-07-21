package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptArtifact
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildIssue
import com.example.phishingawareness.domain.model.PromptBuildIssueCode
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.ResolvedGenerationConfig
import com.example.phishingawareness.domain.prompt.PromptBuilder
import java.security.MessageDigest

/**
 * Implementazione deterministica del prompt builder.
 *
 * Le sezioni vengono assemblate nello stesso ordine in cui sono ricevute.
 * La classe non seleziona parametri, non risolve compatibilità e non
 * accede ad asset, repository, UI o runtime.
 */
class DeterministicPromptBuilder : PromptBuilder {

    override fun build(
        configuration: ResolvedGenerationConfig,
        context: PromptBuildContext
    ): PromptBuildResult {
        val issues = validate(configuration, context)

        if (issues.isNotEmpty()) {
            return PromptBuildResult.Failure(issues)
        }

        val promptText = configuration.sections
            .joinToString(separator = SECTION_SEPARATOR) { section ->
                section.content
            }

        val metadata = PromptMetadata(
            buildContext = context,
            resolvedConfigurationId = configuration.configurationId,
            promptSha256 = sha256(promptText)
        )

        return PromptBuildResult.Success(
            artifact = PromptArtifact(
                text = promptText,
                metadata = metadata
            )
        )
    }

    private fun validate(
        configuration: ResolvedGenerationConfig,
        context: PromptBuildContext
    ): List<PromptBuildIssue> {
        val issues = mutableListOf<PromptBuildIssue>()

        addMissingValueIssue(
            issues = issues,
            field = "configurationId",
            value = configuration.configurationId
        )

        addMissingValueIssue(
            issues = issues,
            field = "language",
            value = configuration.language
        )

        addVersionIssue(issues, "builderVersion", context.builderVersion)
        addVersionIssue(issues, "templateId", context.templateId)
        addVersionIssue(issues, "templateVersion", context.templateVersion)
        addVersionIssue(issues, "libraryId", context.libraryId)
        addVersionIssue(issues, "libraryVersion", context.libraryVersion)

        if (context.librarySchemaVersion <= 0) {
            issues += PromptBuildIssue(
                code = PromptBuildIssueCode.MISSING_VERSION_METADATA,
                field = "librarySchemaVersion"
            )
        }

        if (configuration.sections.isEmpty()) {
            issues += PromptBuildIssue(
                code = PromptBuildIssueCode.MISSING_REQUIRED_SECTION,
                field = "sections"
            )
        }

        configuration.sections.forEachIndexed { index, section ->
            if (section.id.isBlank()) {
                issues += PromptBuildIssue(
                    code = PromptBuildIssueCode.MISSING_REQUIRED_VALUE,
                    field = "sections[$index].id"
                )
            }

            if (section.content.isBlank()) {
                issues += PromptBuildIssue(
                    code = PromptBuildIssueCode.MISSING_REQUIRED_VALUE,
                    field = "sections[$index].content"
                )
            }
        }

        configuration.sections
            .map { it.id }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .forEach { duplicatedId ->
                issues += PromptBuildIssue(
                    code = PromptBuildIssueCode.DUPLICATE_IDENTIFIER,
                    field = "sections",
                    details = duplicatedId
                )
            }

        return issues
    }

    private fun addMissingValueIssue(
        issues: MutableList<PromptBuildIssue>,
        field: String,
        value: String
    ) {
        if (value.isBlank()) {
            issues += PromptBuildIssue(
                code = PromptBuildIssueCode.MISSING_REQUIRED_VALUE,
                field = field
            )
        }
    }

    private fun addVersionIssue(
        issues: MutableList<PromptBuildIssue>,
        field: String,
        value: String
    ) {
        if (value.isBlank()) {
            issues += PromptBuildIssue(
                code = PromptBuildIssueCode.MISSING_VERSION_METADATA,
                field = field
            )
        }
    }

    private fun sha256(value: String): String {
        return MessageDigest
            .getInstance(SHA_256)
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte ->
                "%02x".format(byte.toInt() and 0xff)
            }
    }

    private companion object {
        const val SECTION_SEPARATOR = "\n\n"
        const val SHA_256 = "SHA-256"
    }
}