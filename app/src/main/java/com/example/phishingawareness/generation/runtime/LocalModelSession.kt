package com.example.phishingawareness.generation.runtime

/**
 * Gestisce il ciclo di vita applicativo del modello locale
 * e del relativo context di inferenza.
 *
 * Non esegue generazioni e non costruisce prompt.
 */
interface LocalModelSession {

    fun prepare(
        modelPath: String,
        contextSize: Int
    ): LocalModelSessionResult

    fun release(): LocalModelSessionResult

    fun isReady(): Boolean
}