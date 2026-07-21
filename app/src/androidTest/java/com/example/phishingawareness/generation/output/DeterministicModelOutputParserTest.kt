package com.example.phishingawareness.generation.output

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.ModelOutputParseRequest
import com.example.phishingawareness.domain.model.ModelOutputParseResult
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeterministicModelOutputParserTest {

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
        DeterministicModelOutputParser(
            libraryRepository = repository
        )

    @Test
    fun parse_validBankingOutput_mapsPromptIdsToInternalIds() {
        val result =
            parser.parse(
                ModelOutputParseRequest(
                    rawOutput = validBankingOutput(),
                    expectedScenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is ModelOutputParseResult.Success
        )

        val email =
            (result as ModelOutputParseResult.Success)
                .email

        assertEquals(
            "Servizio Sicurezza Banca Esempio",
            email.senderName
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
    fun parse_emptyOutput_returnsStructuredFailure() {
        val result =
            parser.parse(
                ModelOutputParseRequest(
                    rawOutput = " ",
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as ModelOutputParseResult.Failure

        assertEquals(
            ModelOutputParseIssueCode.EMPTY_OUTPUT,
            failure.issues.single().code
        )
    }

    @Test
    fun parse_textAroundJson_returnsBoundaryFailure() {
        val result =
            parser.parse(
                ModelOutputParseRequest(
                    rawOutput =
                        "Risposta del modello: ${validBankingOutput()}",
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as ModelOutputParseResult.Failure

        assertEquals(
            ModelOutputParseIssueCode
                .INVALID_JSON_BOUNDARY,
            failure.issues.single().code
        )
    }

    @Test
    fun parse_unknownPromptIndicator_returnsStructuredFailure() {
        val output =
            validBankingOutput().replace(
                "IND_URGENCY",
                "IND_UNKNOWN"
            )

        val result =
            parser.parse(
                ModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as ModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                        ModelOutputParseIssueCode
                            .UNKNOWN_PROMPT_INDICATOR
            }
        )
    }

    @Test
    fun parse_duplicatePromptIndicator_returnsStructuredFailure() {
        val output =
            validBankingOutput().replace(
                "IND_SUSPICIOUS_LINK",
                "IND_URGENCY"
            )

        val result =
            parser.parse(
                ModelOutputParseRequest(
                    rawOutput = output,
                    expectedScenario = Scenario.BANKING
                )
            )

        val failure =
            result as ModelOutputParseResult.Failure

        assertTrue(
            failure.issues.any { issue ->
                issue.code ==
                        ModelOutputParseIssueCode
                            .DUPLICATE_PROMPT_INDICATOR
            }
        )
    }

    private fun validBankingOutput(): String {
        return """
            {
              "scenario": "Bancario e pagamenti",
              "difficulty": "Media",
              "length": "Media",
              "sender_name": "Servizio Sicurezza Banca Esempio",
              "sender_address": "sicurezza@bancaesempio.invalid",
              "recipient": "utente@bancaesempio.invalid",
              "subject": "Verifica accesso sospetto",
              "body": "Gentile cliente, abbiamo rilevato un accesso sospetto. Accedi immediatamente per evitare la sospensione temporanea del conto. Effettua il login con il tuo nome utente e la tua password per verificare l'accesso. https://bancaesempio.invalid/login",
              "pretext": "Accesso sospetto",
              "cta_type": "LOGIN",
              "cta_text": "Accedi al conto",
              "present_indicators": [
                {
                  "id": "IND_URGENCY",
                  "evidence": "Accedi immediatamente per evitare la sospensione temporanea del conto.",
                  "explanation": "La frase crea pressione temporale."
                },
                {
                  "id": "IND_SUSPICIOUS_LINK",
                  "evidence": "https://bancaesempio.invalid/login",
                  "explanation": "Il collegamento usa un dominio fittizio."
                },
                {
                  "id": "IND_CREDENTIAL_REQUEST",
                  "evidence": "Effettua il login con il tuo nome utente e la tua password per verificare l'accesso.",
                  "explanation": "La frase richiede credenziali."
                }
              ],
              "credibility_elements": [
                "tono professionale",
                "pretesto plausibile",
                "firma del servizio",
                "riferimento alla sicurezza del conto"
              ],
              "educational_summary": "Controllare mittente, collegamenti e richieste di credenziali."
            }
        """.trimIndent()
    }
}