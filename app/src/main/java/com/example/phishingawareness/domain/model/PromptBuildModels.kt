package com.example.phishingawareness.domain.model

/**
 * Informazioni di versione necessarie per costruire un prompt
 * deterministico e tracciabile.
 *
 * Non contiene dati di inferenza o output del modello.
 */
data class PromptBuildContext(
    val builderVersion: String,
    val templateId: String,
    val templateVersion: String,
    val libraryId: String,
    val libraryVersion: String,
    val librarySchemaVersion: Int
)

sealed interface PromptBuildResult {

    data class Success(
        val artifact: PromptArtifact
    ) : PromptBuildResult

    data class Failure(
        val issues: List<PromptBuildIssue>
    ) : PromptBuildResult
}

data class PromptArtifact(
    val text: String,
    val metadata: PromptMetadata
)

data class PromptMetadata(
    val buildContext: PromptBuildContext,
    val resolvedConfigurationId: String,
    val promptSha256: String
)

data class PromptBuildIssue(
    val code: PromptBuildIssueCode,
    val field: String? = null,
    val details: String? = null
)

enum class PromptBuildIssueCode {
    MISSING_REQUIRED_VALUE,
    DUPLICATE_IDENTIFIER,
    MISSING_VERSION_METADATA,
    ASSEMBLY_CONFLICT,
    MISSING_REQUIRED_SECTION,
    INVALID_SECTION_ORDER
}