package com.example.phishingawareness.generation.runtime

/**
 * Risultato tipizzato di una generazione eseguita dal runtime nativo.
 *
 * rawText contiene il testo grezzo prodotto dal modello, prima di:
 * - estrazione del JSON;
 * - parsing del contenuto;
 * - validazione osservazionale;
 * - collegamento al quiz.
 */
data class NativeGeneratedSequence(
    val requestedTokenCount: Int,
    val generatedTokenCount: Int,
    val reachedEndOfGeneration: Boolean,
    val tokenIds: List<Int>,
    val rawText: String
)