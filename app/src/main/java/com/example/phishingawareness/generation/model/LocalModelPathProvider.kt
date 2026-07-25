package com.example.phishingawareness.generation.model

/**
 * Individua e verifica il file del modello locale.
 *
 * Il contratto non carica il modello e non interagisce con JNI.
 */
fun interface LocalModelPathProvider {

    fun resolve(): LocalModelPathResult
}