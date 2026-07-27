package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.model.Scenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gemma1BCompactRuntimePromptSectionResolverTest {

    private val resolver =
        Gemma1BCompactRuntimePromptSectionResolver()

    @Test
    fun resolve_accountItParameters_returnsOrderedCompactSections() {
        val result =
            resolver.resolve(
                accountItParameters()
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
    fun resolve_accountItParameters_preservesRequiredConstraints() {
        val result =
            resolver.resolve(
                accountItParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val prompt =
            result.configuration.sections
                .joinToString("\n\n") { section ->
                    section.content
                }

        assertTrue(
            prompt.contains(
                "supporto@aziendaesempio.invalid"
            )
        )

        assertTrue(
            prompt.contains(
                "utente@aziendaesempio.invalid"
            )
        )

        assertTrue(
            prompt.contains(
                "Aggiorna la password immediatamente " +
                        "per evitare il blocco dell'account."
            )
        )

        assertTrue(
            prompt.contains(
                "Accedi con il tuo nome utente e la tua password " +
                        "per completare l'aggiornamento."
            )
        )

        assertTrue(
            prompt.contains(
                "IND_URGENCY,IND_SUSPICIOUS_LINK," +
                        "IND_CREDENTIAL_REQUEST"
            )
        )

        assertFalse(
            prompt.contains(
                "\"string\""
            )
        )
    }

    @Test
    fun resolve_accountItParameters_isSubstantiallyShorterThanStandard() {
        val standardResult =
            DeterministicRuntimePromptSectionResolver()
                .resolve(
                    accountItParameters()
                ) as RuntimePromptSectionResolutionResult.Success

        val compactResult =
            resolver.resolve(
                accountItParameters()
            ) as RuntimePromptSectionResolutionResult.Success

        val standardLength =
            standardResult.configuration.sections
                .sumOf { section ->
                    section.content.length
                }

        val compactLength =
            compactResult.configuration.sections
                .sumOf { section ->
                    section.content.length
                }

        assertTrue(
            "Il resolver compatto non riduce abbastanza il prompt: " +
                    "standard=$standardLength, compact=$compactLength",
            compactLength < standardLength * 0.65
        )
    }

    @Test
    fun resolve_invalidParameters_preservesStandardValidationFailure() {
        val result =
            resolver.resolve(
                accountItParameters().copy(
                    requiredIndicatorPromptIds = emptyList()
                )
            )

        assertTrue(
            result is RuntimePromptSectionResolutionResult.Failure
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