package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildIssueCode
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.ResolvedGenerationConfig
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class DeterministicPromptBuilderTest {

    private val builder = DeterministicPromptBuilder()

    @Test
    fun build_withValidConfiguration_preservesSectionOrder() {
        val result = builder.build(
            configuration = validConfiguration(),
            context = validContext()
        )

        assertTrue(result is PromptBuildResult.Success)

        val success = result as PromptBuildResult.Success

        assertEquals(
            "Prima sezione\n\nSeconda sezione",
            success.artifact.text
        )
    }

    @Test
    fun build_withSameInput_returnsSameTextAndHash() {
        val configuration = validConfiguration()
        val context = validContext()

        val firstResult = builder.build(
            configuration = configuration,
            context = context
        )

        val secondResult = builder.build(
            configuration = configuration,
            context = context
        )

        assertTrue(firstResult is PromptBuildResult.Success)
        assertTrue(secondResult is PromptBuildResult.Success)

        val firstSuccess = firstResult as PromptBuildResult.Success
        val secondSuccess = secondResult as PromptBuildResult.Success

        assertEquals(
            firstSuccess.artifact.text,
            secondSuccess.artifact.text
        )

        assertEquals(
            firstSuccess.artifact.metadata.promptSha256,
            secondSuccess.artifact.metadata.promptSha256
        )

        assertEquals(
            sha256(firstSuccess.artifact.text),
            firstSuccess.artifact.metadata.promptSha256
        )
    }

    @Test
    fun build_withMissingRequiredValues_returnsFailure() {
        val configuration = validConfiguration().copy(
            configurationId = "",
            language = "",
            sections = emptyList()
        )

        val context = validContext().copy(
            builderVersion = "",
            templateId = "",
            librarySchemaVersion = 0
        )

        val result = builder.build(
            configuration = configuration,
            context = context
        )

        assertTrue(result is PromptBuildResult.Failure)

        val failure = result as PromptBuildResult.Failure
        val issueCodes = failure.issues.map { it.code }

        assertTrue(
            issueCodes.contains(
                PromptBuildIssueCode.MISSING_REQUIRED_VALUE
            )
        )

        assertTrue(
            issueCodes.contains(
                PromptBuildIssueCode.MISSING_VERSION_METADATA
            )
        )

        assertTrue(
            issueCodes.contains(
                PromptBuildIssueCode.MISSING_REQUIRED_SECTION
            )
        )
    }

    @Test
    fun build_withDuplicateSectionIds_returnsFailure() {
        val configuration = validConfiguration().copy(
            sections = listOf(
                ResolvedPromptSection(
                    id = "RULES",
                    content = "Prima regola"
                ),
                ResolvedPromptSection(
                    id = "RULES",
                    content = "Seconda regola"
                )
            )
        )

        val result = builder.build(
            configuration = configuration,
            context = validContext()
        )

        assertTrue(result is PromptBuildResult.Failure)

        val failure = result as PromptBuildResult.Failure

        val duplicateIssue = failure.issues.firstOrNull {
            it.code == PromptBuildIssueCode.DUPLICATE_IDENTIFIER
        }

        assertEquals("RULES", duplicateIssue?.details)
    }

    @Test
    fun build_withBlankSectionFields_returnsFailure() {
        val configuration = validConfiguration().copy(
            sections = listOf(
                ResolvedPromptSection(
                    id = "",
                    content = "Contenuto valido"
                ),
                ResolvedPromptSection(
                    id = "EMPTY_CONTENT",
                    content = ""
                )
            )
        )

        val result = builder.build(
            configuration = configuration,
            context = validContext()
        )

        assertTrue(result is PromptBuildResult.Failure)

        val failure = result as PromptBuildResult.Failure

        assertTrue(
            failure.issues.any {
                it.code == PromptBuildIssueCode.MISSING_REQUIRED_VALUE &&
                        it.field == "sections[0].id"
            }
        )

        assertTrue(
            failure.issues.any {
                it.code == PromptBuildIssueCode.MISSING_REQUIRED_VALUE &&
                        it.field == "sections[1].content"
            }
        )
    }

    private fun validConfiguration(): ResolvedGenerationConfig {
        return ResolvedGenerationConfig(
            configurationId = "BANKING_MEDIUM_MEDIUM_01",
            scenario = Scenario.BANKING,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it",
            sections = listOf(
                ResolvedPromptSection(
                    id = "FIRST",
                    content = "Prima sezione"
                ),
                ResolvedPromptSection(
                    id = "SECOND",
                    content = "Seconda sezione"
                )
            )
        )
    }

    private fun validContext(): PromptBuildContext {
        return PromptBuildContext(
            builderVersion = "1",
            templateId = "BANKING_ZERO_SHOT",
            templateVersion = "12",
            libraryId = "phishing-awareness-library",
            libraryVersion = "1.0",
            librarySchemaVersion = 1
        )
    }

    private fun sha256(value: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte ->
                "%02x".format(byte.toInt() and 0xff)
            }
    }
}