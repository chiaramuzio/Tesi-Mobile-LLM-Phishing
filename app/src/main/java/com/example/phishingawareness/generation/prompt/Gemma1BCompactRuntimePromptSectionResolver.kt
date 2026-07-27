package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.prompt.RuntimePromptSectionResolver

class Gemma1BCompactRuntimePromptSectionResolver(
    private val validationDelegate: RuntimePromptSectionResolver =
        DeterministicRuntimePromptSectionResolver()
) : RuntimePromptSectionResolver {

    override fun resolve(
        parameters: ResolvedPromptParameters
    ): RuntimePromptSectionResolutionResult {
        val validatedResult =
            validationDelegate.resolve(parameters)

        if (
            validatedResult
                    !is RuntimePromptSectionResolutionResult.Success
        ) {
            return validatedResult
        }

        return RuntimePromptSectionResolutionResult.Success(
            configuration =
                validatedResult.configuration.copy(
                    sections = compactSections(parameters)
                )
        )
    }

    private fun compactSections(
        parameters: ResolvedPromptParameters
    ): List<ResolvedPromptSection> {
        return listOf(
            section(
                id = SECTION_ROLE_AND_OBJECTIVE,
                content =
                    "Genera una email simulata di phishing educativo."
            ),
            section(
                id = SECTION_PARAMETERS,
                content = compactParameters(parameters)
            ),
            section(
                id = SECTION_REQUIRED_INDICATORS,
                content =
                    "Indicatori obbligatori, esattamente questi: " +
                            parameters.requiredIndicatorPromptIds
                                .joinToString(",")
            ),
            section(
                id = SECTION_CREDIBILITY_ELEMENTS,
                content =
                    "Credibilità richiesta, stesso ordine: " +
                            parameters.credibilityElements
                                .joinToString(" | ")
            ),
            section(
                id = SECTION_COMMON_RULES,
                content = COMMON_RULES
            ),
            section(
                id = SECTION_SCENARIO_RULES,
                content = scenarioRules(parameters.scenario)
            ),
            section(
                id = SECTION_INTERNAL_CHECKS,
                content = INTERNAL_CHECKS
            ),
            section(
                id = SECTION_OUTPUT_FORMAT,
                content = Gemma1BCompactOutputContract.content
            )
        )
    }

    private fun compactParameters(
        parameters: ResolvedPromptParameters
    ): String {
        return buildString {
            append("scenario=")
            append(parameters.scenario.name)
            append(";difficulty=")
            append(parameters.difficulty.name)
            append(";length=")
            append(parameters.length.name)
            append(";pretext=")
            append(parameters.pretext)
            append(";brand=")
            append(parameters.brandName)
        }
    }

    private fun scenarioRules(
        scenario: Scenario
    ): String {
        return when (scenario) {
            Scenario.BANKING -> BANKING_RULES
            Scenario.ACCOUNT_IT -> ACCOUNT_IT_RULES
        }
    }

    private fun section(
        id: String,
        content: String
    ): ResolvedPromptSection {
        return ResolvedPromptSection(
            id = id,
            content = content
        )
    }

    private companion object {

        const val COMMON_RULES =
            "Italiano. Solo dati fittizi. Nessun telefono. " +
                    "Una CTA. Tre indicatori. " +
                    "Evidence letterali nel body. Nessun distrattore."

        const val BANKING_RULES =
            "Usa Banca Esempio e solo bancaesempio.invalid. " +
                    "sender_name=Servizio Sicurezza Banca Esempio; " +
                    "sender_address=sicurezza@bancaesempio.invalid; " +
                    "recipient=utente@bancaesempio.invalid; " +
                    "cta_type=LOGIN; cta_text=Accedi al conto. " +
                    "Nel body inserisci esattamente: " +
                    "Accedi immediatamente per evitare la sospensione temporanea del conto. " +
                    "Poi: Effettua il login con il tuo nome utente e la tua password per verificare l'accesso. " +
                    "Inserisci https://bancaesempio.invalid/login su una riga separata. " +
                    "Usa queste tre stringhe come evidence dei rispettivi indicatori. " +
                    "Termina con Cordiali saluti e Servizio Sicurezza Banca Esempio."

        const val ACCOUNT_IT_RULES =
            "sender_name=Supporto IT; " +
                    "sender_address=supporto@aziendaesempio.invalid; " +
                    "recipient=utente@aziendaesempio.invalid; " +
                    "cta_type=LOGIN; cta_text=Aggiorna la password; " +
                    "URL=https://aziendaesempio.invalid. " +
                    "Nel body usa esattamente: " +
                    "Aggiorna la password immediatamente per evitare il blocco dell'account. " +
                    "Accedi con il tuo nome utente e la tua password per completare l'aggiornamento. " +
                    "Inserisci anche l'URL esatto. " +
                    "Le tre evidence sono, nello stesso ordine: prima frase, URL, seconda frase. " +
                    "Termina con Supporto IT."

        const val INTERNAL_CHECKS =
            "Controlla: JSON completo, valori esatti, " +
                    "3 indicatori, evidence nel body."

        const val SECTION_ROLE_AND_OBJECTIVE =
            "ROLE_AND_OBJECTIVE"

        const val SECTION_PARAMETERS =
            "PARAMETERS"

        const val SECTION_REQUIRED_INDICATORS =
            "REQUIRED_INDICATORS"

        const val SECTION_CREDIBILITY_ELEMENTS =
            "CREDIBILITY_ELEMENTS"

        const val SECTION_COMMON_RULES =
            "COMMON_RULES"

        const val SECTION_SCENARIO_RULES =
            "SCENARIO_RULES"

        const val SECTION_INTERNAL_CHECKS =
            "INTERNAL_CHECKS"

        const val SECTION_OUTPUT_FORMAT =
            "OUTPUT_FORMAT"
    }
}