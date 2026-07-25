package com.example.phishingawareness.generation.model

import com.example.phishingawareness.generation.runtime.LocalModelSessionResult

/**
 * Prepara il modello locale risolvendo prima il file GGUF
 * e inizializzando poi la sessione nativa.
 *
 * Non esegue generazioni e non costruisce prompt.
 */
interface LocalModelBootstrap {

    fun prepare(
        contextSize: Int
    ): LocalModelBootstrapResult

    fun release(): LocalModelSessionResult

    fun isReady(): Boolean
}