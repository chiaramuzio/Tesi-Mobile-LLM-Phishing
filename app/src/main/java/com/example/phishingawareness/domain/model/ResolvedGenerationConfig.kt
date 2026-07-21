package com.example.phishingawareness.domain.model

/**
 * Contratto di una configurazione di generazione già risolta e validata
 * dai componenti precedenti al prompt builder.
 *
 * Il prompt builder non deve usare questo tipo per selezionare parametri
 * o risolvere compatibilità.
 *
 * Il modello concreto dei parametri verrà completato nel prossimo blocco.
 */
interface ResolvedGenerationConfig {
    val configurationId: String
}