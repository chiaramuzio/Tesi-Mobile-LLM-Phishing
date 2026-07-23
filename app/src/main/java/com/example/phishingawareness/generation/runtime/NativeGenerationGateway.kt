package com.example.phishingawareness.generation.runtime

/**
 * Punto di accesso sostituibile alla generazione JNI.
 *
 * Consente di testare l'adattatore applicativo senza caricare
 * la libreria nativa o il modello reale.
 */
fun interface NativeGenerationGateway {

    fun generateConfiguredSequenceResult(
        prompt: String,
        addSpecial: Boolean,
        parameters: LocalGenerationParameters
    ): NativeGeneratedSequence
}