package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptGenerationRequest
import com.example.phishingawareness.domain.model.PromptGenerationResult

/**
 * Coordina l'intera pipeline di generazione del prompt.
 *
 * Le implementazioni concrete gestiscono caricamento del template,
 * risoluzione della configurazione e costruzione dell'artefatto finale.
 */
interface PromptGenerationOrchestrator {

    fun generate(
        request: PromptGenerationRequest
    ): PromptGenerationResult
}