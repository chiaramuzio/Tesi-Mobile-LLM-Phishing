package com.example.phishingawareness.generation.runtime

/**
 * Contratto applicativo per una singola generazione locale.
 *
 * Il chiamante fornisce un prompt già costruito e una configurazione
 * di sampling già risolta.
 *
 * Il contratto non:
 * - costruisce il prompt;
 * - legge la libreria Excel;
 * - estrae o interpreta JSON;
 * - valida il contenuto dell'email;
 * - rigenera automaticamente l'output.
 */
fun interface LocalGenerationRuntime {

    fun generate(
        prompt: String,
        parameters: LocalGenerationParameters
    ): NativeGeneratedSequence
}