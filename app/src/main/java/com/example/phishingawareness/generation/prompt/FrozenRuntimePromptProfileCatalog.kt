package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.RuntimePromptProfile
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.prompt.RuntimePromptProfileCatalog

object FrozenRuntimePromptProfileCatalog :
    RuntimePromptProfileCatalog {

    private val bankingMediumMedium =
        RuntimePromptProfile(
            id = "BANKING_MEDIUM_MEDIUM_V1",
            scenario = Scenario.BANKING,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            scenarioLabel = "Bancario e pagamenti",
            pretext = "Accesso sospetto",
            impersonatedIdentity =
                "Servizio di sicurezza di un istituto bancario fittizio",
            brandName = "Banca Esempio",
            ctaType = "LOGIN",
            requiredIndicatorIds = listOf(
                "URGENCY_PRESSURE",
                "SUSPICIOUS_LINK",
                "SENSITIVE_DATA_REQUEST"
            ),
            credibilityElements = listOf(
                "tono professionale",
                "pretesto plausibile",
                "firma del servizio",
                "riferimento alla sicurezza del conto"
            )
        )

    private val accountItMediumMedium =
        RuntimePromptProfile(
            id = "ACCOUNT_IT_MEDIUM_MEDIUM_V1",
            scenario = Scenario.ACCOUNT_IT,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            scenarioLabel =
                "Account aziendale, webmail e servizi IT",
            pretext = "Password in scadenza",
            impersonatedIdentity =
                "Servizio IT aziendale",
            brandName = "Azienda Esempio",
            ctaType = "LOGIN",
            requiredIndicatorIds = listOf(
                "URGENCY_PRESSURE",
                "SUSPICIOUS_LINK",
                "SENSITIVE_DATA_REQUEST"
            ),
            credibilityElements = listOf(
                "tono professionale",
                "firma del servizio IT",
                "riferimento a policy aziendali",
                "linguaggio tecnico plausibile"
            )
        )

    private val profiles = listOf(
        bankingMediumMedium,
        accountItMediumMedium
    )

    override fun get(
        scenario: Scenario,
        difficulty: Difficulty,
        length: ExerciseLength
    ): RuntimePromptProfile? {
        return profiles.firstOrNull { profile ->
            profile.scenario == scenario &&
                    profile.difficulty == difficulty &&
                    profile.length == length
        }
    }
}