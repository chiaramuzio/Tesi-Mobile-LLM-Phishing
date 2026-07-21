package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.ResolvedGenerationConfig

/**
 * Costruisce un prompt deterministico a partire da una configurazione
 * già risolta e compatibile.
 *
 * Le implementazioni non devono:
 * - leggere la libreria Excel o gli asset Android;
 * - risolvere compatibilità;
 * - selezionare parametri;
 * - eseguire il modello;
 * - analizzare l'output;
 * - costruire il quiz.
 */
interface PromptBuilder {

    fun build(
        configuration: ResolvedGenerationConfig,
        context: PromptBuildContext
    ): PromptBuildResult
}