package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult

/**
 * Carica un template operativo completo a partire dal suo identificativo.
 */
interface PromptTemplateLoader {

    fun load(
        templateId: PromptTemplateId
    ): PromptTemplateLoadResult
}
