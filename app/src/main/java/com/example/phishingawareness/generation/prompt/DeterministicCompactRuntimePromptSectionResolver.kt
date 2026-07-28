package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.prompt.RuntimePromptSectionResolver

class DeterministicCompactRuntimePromptSectionResolver(
    private val completeResolver: RuntimePromptSectionResolver =
        DeterministicRuntimePromptSectionResolver()
) : RuntimePromptSectionResolver {

    override fun resolve(
        parameters: ResolvedPromptParameters
    ): RuntimePromptSectionResolutionResult {
        return when (
            val result = completeResolver.resolve(parameters)
        ) {
            is RuntimePromptSectionResolutionResult.Failure ->
                result

            is RuntimePromptSectionResolutionResult.Success ->
                RuntimePromptSectionResolutionResult.Success(
                    configuration =
                        result.configuration.copy(
                            sections =
                                result.configuration.sections.map { section ->
                                    if (section.id == SECTION_OUTPUT_FORMAT) {
                                        compactOutputFormatSection()
                                    } else {
                                        section
                                    }
                                }
                        )
                )
        }
    }

    private fun compactOutputFormatSection():
        ResolvedPromptSection {
        return ResolvedPromptSection(
            id = SECTION_OUTPUT_FORMAT,
            content = """
                FORMATO OBBLIGATORIO
                Restituisci esclusivamente un singolo oggetto JSON valido.
                Non usare Markdown, delimitatori ``` o testo prima o dopo il JSON.
                Usa esattamente questa struttura:
                {
                  "sender_name": "string",
                  "sender_address": "string",
                  "subject": "string",
                  "body": "string",
                  "present_indicators": [
                    {
                      "id": "string",
                      "evidence": "string"
                    }
                  ]
                }

                In present_indicators inserisci esclusivamente gli indicatori richiesti.
                Per ogni indicatore:
                - id deve coincidere esattamente con uno degli ID richiesti;
                - evidence deve essere una citazione letterale presente nel body.
                Non aggiungere altri campi.
            """.trimIndent()
        )
    }

    private companion object {
        const val SECTION_OUTPUT_FORMAT =
            "OUTPUT_FORMAT"
    }
}
