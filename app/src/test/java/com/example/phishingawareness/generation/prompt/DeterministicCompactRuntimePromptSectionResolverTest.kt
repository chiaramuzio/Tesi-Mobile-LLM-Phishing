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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicCompactRuntimePromptSectionResolverTest {

    private val completeResolver =
        DeterministicRuntimePromptSectionResolver()

    private val compactResolver =
        DeterministicCompactRuntimePromptSectionResolver()

    @Test
    fun resolve_bankingParameters_preservesSectionOrder() {
        val result =
            compactResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

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
            result.configuration.sections.map { section ->
                section.id
            }
        )
    }

    @Test
    fun resolve_replacesOnlyOutputFormatSection() {
        val completeResult =
            completeResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val compactResult =
            compactResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val completeNonOutputSections =
            completeResult.configuration.sections
                .filterNot { section ->
                    section.id == "OUTPUT_FORMAT"
                }

        val compactNonOutputSections =
            compactResult.configuration.sections
                .filterNot { section ->
                    section.id == "OUTPUT_FORMAT"
                }

        assertEquals(
            completeNonOutputSections,
            compactNonOutputSections
        )

        assertFalse(
            completeResult.configuration.sections
                .single { it.id == "OUTPUT_FORMAT" }
                .content ==
                compactResult.configuration.sections
                    .single { it.id == "OUTPUT_FORMAT" }
                    .content
        )
    }

    @Test
    fun resolve_addsExpectedCompactOutputFormat() {
        val result =
            compactResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val output =
            result.configuration.sections
                .single { section ->
                    section.id == "OUTPUT_FORMAT"
                }
                .content

        assertTrue(output.contains("\"sender_name\""))
        assertTrue(output.contains("\"sender_address\""))
        assertTrue(output.contains("\"subject\""))
        assertTrue(output.contains("\"body\""))
        assertTrue(output.contains("\"present_indicators\""))
        assertTrue(output.contains("\"id\""))
        assertTrue(output.contains("\"evidence\""))

        assertFalse(output.contains("\"scenario\""))
        assertFalse(output.contains("\"difficulty\""))
        assertFalse(output.contains("\"length\""))
        assertFalse(output.contains("\"recipient\""))
        assertFalse(output.contains("\"pretext\""))
        assertFalse(output.contains("\"cta_type\""))
        assertFalse(output.contains("\"cta_text\""))
        assertFalse(output.contains("\"explanation\""))
        assertFalse(output.contains("\"credibility_elements\""))
        assertFalse(output.contains("\"educational_summary\""))
    }

    @Test
    fun resolve_requiresJsonWithoutMarkdownOrExtraFields() {
        val result =
            compactResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val output =
            result.configuration.sections
                .single { it.id == "OUTPUT_FORMAT" }
                .content

        assertTrue(
            output.contains(
                "Restituisci esclusivamente un singolo oggetto JSON valido."
            )
        )

        assertTrue(
            output.contains(
                "Non usare Markdown"
            )
        )

        assertTrue(
            output.contains(
                "Non aggiungere altri campi."
            )
        )

        assertTrue(
            output.contains(
                "evidence deve essere una citazione letterale presente nel body."
            )
        )
    }

    @Test
    fun resolve_preservesValidationFailures() {
        val result =
            compactResolver.resolve(
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

    @Test
    fun resolvedCompactSections_canBeBuiltDeterministically() {
        val sectionResult =
            compactResolver.resolve(
                bankingParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val buildResult =
            DeterministicPromptBuilder().build(
                configuration = sectionResult.configuration,
                context = PromptBuildContext(
                    builderVersion = "1",
                    templateId = "RUNTIME_MODULAR_COMPACT",
                    templateVersion = "1",
                    libraryId =
                        "phishing-awareness-library",
                    libraryVersion = "0.3.0",
                    librarySchemaVersion = 2
                )
            )

        assertTrue(
            buildResult is PromptBuildResult.Success
        )

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
            artifact.text.contains(
                "\"present_indicators\""
            )
        )

        assertFalse(
            artifact.text.contains(
                "\"educational_summary\""
            )
        )

        assertEquals(
            64,
            artifact.metadata.promptSha256.length
        )
    }

    @Test
    fun resolve_accountItParameters_preservesScenarioRules() {
        val result =
            compactResolver.resolve(
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

    private fun bankingParameters():
        ResolvedPromptParameters {
        return ResolvedPromptParameters(
            configurationId = "configuration-compact-banking",
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

    private fun accountItParameters():
        ResolvedPromptParameters {
        return ResolvedPromptParameters(
            configurationId = "configuration-compact-account-it",
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
