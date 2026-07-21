package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult

/**
 * Trasforma un template operativo caricato in una configurazione
 * risolta e pronta per il PromptBuilder.
 */
interface PromptTemplateResolver {

    fun resolve(
        request: PromptTemplateResolutionRequest
    ): PromptTemplateResolutionResult
}