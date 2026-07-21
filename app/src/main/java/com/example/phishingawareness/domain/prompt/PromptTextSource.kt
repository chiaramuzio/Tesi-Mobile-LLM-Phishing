package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptTextReadResult

/**
 * Fornisce il testo di un prompt a partire dal suo percorso logico.
 *
 * Il contratto non conosce Android, AssetManager o il file system.
 * Le implementazioni concrete decidono da dove leggere il contenuto.
 */
interface PromptTextSource {

    fun read(
        assetPath: String
    ): PromptTextReadResult
}