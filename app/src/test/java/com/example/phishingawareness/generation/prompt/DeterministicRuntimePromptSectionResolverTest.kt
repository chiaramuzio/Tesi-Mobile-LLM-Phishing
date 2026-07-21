package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionIssueCode
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicRuntimePromptSectionResolverTest {

    private val resolver =
        DeterministicRuntimePromptSectionResolver()

    @Test
    fun resolve_bankingParameters_returnsOrderedSections() {
        val result =
            resolver.resolve(
                bankingParameters()
            )

        assertTrue(
            result is RuntimePromptSectionResolutionResult.Success
        )

        val configuration =
            (result as RuntimePromptSectionResolutionResult.Success)
                .configuration

        assertEquals(
            listOf(
                "ROLE_AND_OBJECTIVE",
                "PARAMETERS",
                "REQUIRED_INDICATORS",
                "CREDIBILITY_ELEMENTS",
                "COMMON_RULES",
                "SCENARIO_RULES",
                "INTERNAL_CHECKS",
                "OUTPUT_FORMAT"
            ),
            configuration.sections.map { section ->
                section.id
            }
        )
    }

    @Test
    fun resolve_bankingParameters_buildsExpectedParameterSection() {
        val result =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val section =
            result.configuration.sections
                .single { it.id == "PARAMETERS" }

        assertEquals(
            """
                PARAMETRI
                Scenario: Bancario e pagamenti
                Difficoltà: Medium
                Lunghezza: Medium
                Lingua: Italiano
                Pretesto: Accesso sospetto
                Identità impersonata: Servizio di sicurezza di un istituto bancario fittizio
                Marchio nominale: Banca Esempio
                Tipo di call to action: LOGIN
            """.trimIndent(),
            section.content
        )
    }

    @Test
    fun resolve_preservesIndicatorOrder() {
        val result =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val section =
            result.configuration.sections
                .single { it.id == "REQUIRED_INDICATORS" }

        assertEquals(
            """
                INDICATORI RICHIESTI
                - IND_URGENCY
                - IND_SUSPICIOUS_LINK
                - IND_CREDENTIAL_REQUEST
            """.trimIndent(),
            section.content
        )
    }

    @Test
    fun resolve_preservesCredibilityElementOrder() {
        val result =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val section =
            result.configuration.sections
                .single { it.id == "CREDIBILITY_ELEMENTS" }

        assertEquals(
            """
                ELEMENTI DI CREDIBILITÀ RICHIESTI
                - tono professionale
                - pretesto plausibile
                - firma del servizio
                - riferimento alla sicurezza del conto
            """.trimIndent(),
            section.content
        )
    }

    @Test
    fun resolve_duplicateIndicator_returnsStructuredFailure() {
        val parameters =
            bankingParameters().copy(
                requiredIndicatorPromptIds = listOf(
                    "IND_URGENCY",
                    "IND_URGENCY"
                )
            )

        val result = resolver.resolve(parameters)

        assertTrue(
            result is RuntimePromptSectionResolutionResult.Failure
        )

        val failure =
            result as RuntimePromptSectionResolutionResult.Failure

        assertEquals(
            RuntimePromptSectionResolutionIssueCode
                .DUPLICATE_REQUIRED_INDICATOR,
            failure.issues.single().code
        )

        assertEquals(
            "IND_URGENCY",
            failure.issues.single().details
        )
    }

    @Test
    fun resolve_emptyIndicators_returnsStructuredFailure() {
        val result =
            resolver.resolve(
                bankingParameters().copy(
                    requiredIndicatorPromptIds = emptyList()
                )
            )

        val failure =
            result as RuntimePromptSectionResolutionResult.Failure

        assertEquals(
            RuntimePromptSectionResolutionIssueCode
                .EMPTY_REQUIRED_INDICATORS,
            failure.issues.single().code
        )
    }

    private fun bankingParameters(): ResolvedPromptParameters {
        return ResolvedPromptParameters(
            configurationId = "configuration-1",
            profileId = "BANKING_MEDIUM_MEDIUM_V1",
            scenario = Scenario.BANKING,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it",
            scenarioLabel = "Bancario e pagamenti",
            pretext = "Accesso sospetto",
            impersonatedIdentity =
                "Servizio di sicurezza di un istituto bancario fittizio",
            brandName = "Banca Esempio",
            ctaType = "LOGIN",
            requiredIndicatorPromptIds = listOf(
                "IND_URGENCY",
                "IND_SUSPICIOUS_LINK",
                "IND_CREDENTIAL_REQUEST"
            ),
            credibilityElements = listOf(
                "tono professionale",
                "pretesto plausibile",
                "firma del servizio",
                "riferimento alla sicurezza del conto"
            )
        )
    }

    @Test
    fun resolve_addsExpectedOutputFormatSection() {
        val result =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val section =
            result.configuration.sections
                .single { it.id == "OUTPUT_FORMAT" }

        assertEquals(
            """
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
        """.trimIndent(),
            section.content
        )
    }

    @Test
    fun resolvedSections_canBeAssembledByDeterministicBuilder() {
        val sectionResult =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val buildResult =
            DeterministicPromptBuilder().build(
                configuration = sectionResult.configuration,
                context = PromptBuildContext(
                    builderVersion = "1",
                    templateId = "RUNTIME_MODULAR",
                    templateVersion = "1",
                    libraryId = "phishing-awareness-library",
                    libraryVersion = "0.3.0",
                    librarySchemaVersion = 2
                )
            )

        assertTrue(buildResult is PromptBuildResult.Success)

        val artifact =
            (buildResult as PromptBuildResult.Success)
                .artifact

        assertTrue(
            artifact.text.startsWith(
                "Sei un generatore di simulazioni educative"
            )
        )

        assertTrue(
            artifact.text.contains(
                "IND_CREDENTIAL_REQUEST"
            )
        )

        assertTrue(
            artifact.text.endsWith("}")
        )

        assertEquals(
            64,
            artifact.metadata.promptSha256.length
        )
    }

    @Test
    fun resolve_bankingParameters_addsBankingRules() {
        val result =
            resolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val rules =
            result.configuration.sections
                .single { it.id == "SCENARIO_RULES" }
                .content

        assertTrue(
            rules.contains(
                "cta_text deve essere esattamente \"Accedi al conto\"."
            )
        )

        assertTrue(
            rules.contains(
                "https://bancaesempio.invalid/login"
            )
        )

        assertTrue(
            rules.contains(
                "Effettua il login con il tuo nome utente"
            )
        )
    }

    @Test
    fun resolve_accountItParameters_addsAccountItRules() {
        val result =
            resolver.resolve(
                accountItParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val rules =
            result.configuration.sections
                .single { it.id == "SCENARIO_RULES" }
                .content

        assertTrue(
            rules.contains(
                "Aggiorna la password immediatamente"
            )
        )

        assertTrue(
            rules.contains(
                "supporto@aziendaesempio.invalid"
            )
        )

        assertTrue(
            rules.contains(
                "firma esplicita del servizio IT"
            )
        )
    }

    private fun accountItParameters(): ResolvedPromptParameters {
        return ResolvedPromptParameters(
            configurationId = "configuration-2",
            profileId = "ACCOUNT_IT_MEDIUM_MEDIUM_V1",
            scenario = Scenario.ACCOUNT_IT,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it",
            scenarioLabel =
                "Account aziendale, webmail e servizi IT",
            pretext = "Password in scadenza",
            impersonatedIdentity =
                "Servizio IT aziendale",
            brandName = "Azienda Esempio",
            ctaType = "LOGIN",
            requiredIndicatorPromptIds = listOf(
                "IND_URGENCY",
                "IND_SUSPICIOUS_LINK",
                "IND_CREDENTIAL_REQUEST"
            ),
            credibilityElements = listOf(
                "tono professionale",
                "firma del servizio IT",
                "riferimento a policy aziendali",
                "linguaggio tecnico plausibile"
            )
        )
    }
}