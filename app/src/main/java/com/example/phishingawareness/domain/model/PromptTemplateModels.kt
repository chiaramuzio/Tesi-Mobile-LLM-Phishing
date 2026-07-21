package com.example.phishingawareness.domain.model

/**
 * Identifica in modo univoco un template operativo congelato.
 *
 * Ogni valore corrisponde a una baseline scientifica già approvata
 * e non deve essere interpretato come una richiesta di selezione
 * o modifica del template.
 */
enum class PromptTemplateId {
    BANKING_ZERO_SHOT_V12,
    ACCOUNT_IT_ZERO_SHOT_V3
}

/**
 * Rappresenta un template operativo versionato.
 *
 * Le sezioni sono ordinate e devono essere utilizzate nello stesso
 * ordine in cui vengono dichiarate.
 */
data class PromptTemplate(
    val id: PromptTemplateId,
    val version: String,
    val scenario: Scenario,
    val sections: List<PromptTemplateSection>
)

/**
 * Rappresenta una sezione ordinata appartenente a un template.
 *
 * Il contenuto può essere fisso oppure contenere segnaposto che verranno
 * risolti da un componente successivo. Questa classe non esegue alcuna
 * sostituzione o selezione.
 */
data class PromptTemplateSection(
    val id: String,
    val content: String
)

/**
 * Descrive dove è conservato un template operativo.
 *
 * Il riferimento contiene esclusivamente metadati e il percorso relativo
 * del file. Non legge il contenuto dell'asset.
 */
data class PromptTemplateReference(
    val id: PromptTemplateId,
    val version: String,
    val scenario: Scenario,
    val assetPath: String
)