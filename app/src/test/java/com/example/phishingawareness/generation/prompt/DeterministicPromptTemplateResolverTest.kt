package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateResolutionIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult
import com.example.phishingawareness.domain.model.PromptTemplateSection
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicPromptTemplateResolverTest {

    private val resolver =
        DeterministicPromptTemplateResolver()

    @Test
    fun resolve_withValidRequest_returnsExpectedConfiguration() {
        val request = validRequest()

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Success
        )

        val configuration =
            (result as PromptTemplateResolutionResult.Success)
                .configuration

        assertEquals(
            "BANKING_MEDIUM_MEDIUM_IT",
            configuration.configurationId
        )
        assertEquals(
            Scenario.BANKING,
            configuration.scenario
        )
        assertEquals(
            Difficulty.MEDIUM,
            configuration.difficulty
        )
        assertEquals(
            ExerciseLength.MEDIUM,
            configuration.length
        )
        assertEquals(
            "it",
            configuration.language
        )
        assertEquals(
            listOf("ROLE", "OBJECTIVE"),
            configuration.sections.map { section -> section.id }
        )
    }

    @Test
    fun resolve_preservesExactSectionContentAndOrder() {
        val firstContent =
            "  Prima riga  \r\n\r\nSeconda riga\n"

        val secondContent =
            "Testo con accento: credibilità\n"

        val request = validRequest().copy(
            template = validTemplate().copy(
                sections = listOf(
                    PromptTemplateSection(
                        id = "FIRST",
                        content = firstContent
                    ),
                    PromptTemplateSection(
                        id = "SECOND",
                        content = secondContent
                    )
                )
            )
        )

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Success
        )

        val sections =
            (result as PromptTemplateResolutionResult.Success)
                .configuration
                .sections

        assertEquals("FIRST", sections[0].id)
        assertEquals(firstContent, sections[0].content)
        assertEquals("SECOND", sections[1].id)
        assertEquals(secondContent, sections[1].content)
    }

    @Test
    fun resolve_withMissingRequiredValues_returnsIssues() {
        val request = validRequest().copy(
            configurationId = "   ",
            language = "",
            template = validTemplate().copy(
                version = " "
            )
        )

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Failure
        )

        val codes =
            (result as PromptTemplateResolutionResult.Failure)
                .issues
                .map { issue -> issue.code }

        assertTrue(
            codes.contains(
                PromptTemplateResolutionIssueCode
                    .MISSING_CONFIGURATION_ID
            )
        )
        assertTrue(
            codes.contains(
                PromptTemplateResolutionIssueCode
                    .MISSING_LANGUAGE
            )
        )
        assertTrue(
            codes.contains(
                PromptTemplateResolutionIssueCode
                    .MISSING_TEMPLATE_VERSION
            )
        )
    }

    @Test
    fun resolve_withEmptySections_returnsIssue() {
        val request = validRequest().copy(
            template = validTemplate().copy(
                sections = emptyList()
            )
        )

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Failure
        )

        val issues =
            (result as PromptTemplateResolutionResult.Failure)
                .issues

        assertTrue(
            issues.any { issue ->
                issue.code ==
                        PromptTemplateResolutionIssueCode
                            .EMPTY_TEMPLATE_SECTIONS
            }
        )
    }

    @Test
    fun resolve_withBlankSectionValues_returnsExactIssues() {
        val request = validRequest().copy(
            template = validTemplate().copy(
                sections = listOf(
                    PromptTemplateSection(
                        id = " ",
                        content = "Contenuto valido"
                    ),
                    PromptTemplateSection(
                        id = "RULES",
                        content = "\n   \n"
                    )
                )
            )
        )

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Failure
        )

        val issues =
            (result as PromptTemplateResolutionResult.Failure)
                .issues

        assertTrue(
            issues.any { issue ->
                issue.code ==
                        PromptTemplateResolutionIssueCode.EMPTY_SECTION_ID &&
                        issue.field == "template.sections[0].id"
            }
        )

        assertTrue(
            issues.any { issue ->
                issue.code ==
                        PromptTemplateResolutionIssueCode.EMPTY_SECTION_CONTENT &&
                        issue.field == "template.sections[1].content"
            }
        )
    }

    @Test
    fun resolve_withDuplicateSectionIds_returnsIssue() {
        val request = validRequest().copy(
            template = validTemplate().copy(
                sections = listOf(
                    PromptTemplateSection(
                        id = "RULES",
                        content = "Prima sezione"
                    ),
                    PromptTemplateSection(
                        id = "RULES",
                        content = "Seconda sezione"
                    )
                )
            )
        )

        val result = resolver.resolve(request)

        assertTrue(
            result is PromptTemplateResolutionResult.Failure
        )

        val issue =
            (result as PromptTemplateResolutionResult.Failure)
                .issues
                .single { candidate ->
                    candidate.code ==
                            PromptTemplateResolutionIssueCode
                                .DUPLICATE_SECTION_ID
                }

        assertEquals("template.sections", issue.field)
        assertEquals("RULES", issue.details)
    }

    private fun validRequest(): PromptTemplateResolutionRequest {
        return PromptTemplateResolutionRequest(
            configurationId = "BANKING_MEDIUM_MEDIUM_IT",
            template = validTemplate(),
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it"
        )
    }

    private fun validTemplate(): PromptTemplate {
        return PromptTemplate(
            id = PromptTemplateId.BANKING_ZERO_SHOT_V12,
            version = "12",
            scenario = Scenario.BANKING,
            sections = listOf(
                PromptTemplateSection(
                    id = "ROLE",
                    content = "Ruolo del modello"
                ),
                PromptTemplateSection(
                    id = "OBJECTIVE",
                    content = "Obiettivo formativo"
                )
            )
        )
    }
}