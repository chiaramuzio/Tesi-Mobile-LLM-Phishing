package com.example.phishingawareness.domain.model

/**
 * Configurazione completa, compatibile e già risolta prima
 * dell'esecuzione del prompt builder.
 *
 * Il prompt builder non seleziona, modifica o riordina i parametri.
 */
data class ResolvedGenerationConfig(
    val configurationId: String,
    val scenario: Scenario,
    val difficulty: Difficulty,
    val length: ExerciseLength,
    val language: String,
    val sections: List<ResolvedPromptSection>
)

/**
 * Sezione testuale già risolta e pronta per l'assemblaggio.
 *
 * L'ordine delle sezioni nella lista è significativo.
 */
data class ResolvedPromptSection(
    val id: String,
    val content: String
)