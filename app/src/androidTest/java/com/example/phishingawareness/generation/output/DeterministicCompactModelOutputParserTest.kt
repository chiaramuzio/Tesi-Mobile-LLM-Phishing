package com.example.phishingawareness.generation.output

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.domain.model.CompactModelOutputParseRequest
import com.example.phishingawareness.domain.model.CompactModelOutputParseResult
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeterministicCompactModelOutputParserTest {

    private val context =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    private val repository =
        AssetLibraryRepository(
            dataSource =
                LibraryAssetDataSource(context)
        )

    private val parser =
        DeterministicCompactModelOutputParser(
            libraryRepository = repository
        )

    @Test
    fun parse_validCompactBankingOutput_mapsPromptIdsToInternalIds() {
        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = validCompactBankingOutput(),
                    expectedScenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is CompactModelOutputParseResult.Success
        )

        val email =
            (result as CompactModelOutputParseResult.Success)
                .email

        assertEquals(
            "Servizio Sicurezza Banca Esempio",
            email.senderName
        )

        assertEquals(
            "sicurezza@bancaesempio.invalid",
            email.senderAddress
        )

        assertEquals(
            listOf(
                "URGENCY_PRESSURE",
                "SUSPICIOUS_LINK",
                "SENSITIVE_DATA_REQUEST"
            ),
            email.presentIndicators.map { indicator ->
                indicator.internalId
            }
        )
    }

    @Test
    fun parse_outputWithoutExplanation_isAccepted() {
        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = validCompactBankingOutput(),
                    expectedScenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is CompactModelOutputParseResult.Success
        )
    }

    @Test
    fun parse_emptyOutput_returnsStructuredFailure() {
        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = " ",
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertEquals(
            ModelOutputParseIssueCode.EMPTY_OUTPUT,
            failure.issues.single().code
        )
    }

    @Test
    fun parse_textAroundJson_returnsBoundaryFailure() {
        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput =
                        "Risposta: ${validCompactBankingOutput()}",
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertEquals(
            ModelOutputParseIssueCode.INVALID_JSON_BOUNDARY,
            failure.issues.single().code
        )
    }

    @Test
    fun parse_missingSubject_returnsStructuredFailure() {
        val output =
            validCompactBankingOutput().replace(
                """
                "subject": "Verifica accesso sospetto",
                """.trimIndent(),
                ""
            )

        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                    ModelOutputParseIssueCode.MISSING_REQUIRED_FIELD &&
                    issue.field == "subject"
            }
        )
    }

    @Test
    fun parse_unknownPromptIndicator_returnsStructuredFailure() {
        val output =
            validCompactBankingOutput().replace(
                "IND_URGENCY",
                "IND_UNKNOWN"
            )

        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                    ModelOutputParseIssueCode.UNKNOWN_PROMPT_INDICATOR
            }
        )
    }

    @Test
    fun parse_duplicatePromptIndicator_returnsStructuredFailure() {
        val output =
            validCompactBankingOutput().replace(
                "IND_SUSPICIOUS_LINK",
                "IND_URGENCY"
            )

        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                    ModelOutputParseIssueCode.DUPLICATE_PROMPT_INDICATOR
            }
        )
    }

    @Test
    fun parse_emptyEvidence_returnsStructuredFailure() {
        val output =
            validCompactBankingOutput().replace(
                "Evidence urgenza",
                ""
            )

        val result =
            parser.parse(
                CompactModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as CompactModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                    ModelOutputParseIssueCode.EMPTY_REQUIRED_FIELD &&
                    issue.field == "evidence"
            }
        )
    }

    private fun validCompactBankingOutput(): String {
        return """
            {
              "sender_name": "Servizio Sicurezza Banca Esempio",
              "sender_address": "sicurezza@bancaesempio.invalid",
              "subject": "Verifica accesso sospetto",
              "body": "Corpo della simulazione",
              "present_indicators": [
                {
                  "id": "IND_URGENCY",
                  "evidence": "Evidence urgenza"
                },
                {
                  "id": "IND_SUSPICIOUS_LINK",
                  "evidence": "Evidence link"
                },
                {
                  "id": "IND_CREDENTIAL_REQUEST",
                  "evidence": "Evidence credenziali"
                }
              ]
            }
        """.trimIndent()
    }
}
