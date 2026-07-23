package com.example.phishingawareness.generation.runtime

/**
 * Parametri effettivamente passati al runtime locale.
 *
 * Questo modello descrive il sampling applicativo e non contiene
 * dettagli specifici di JNI o llama.cpp.
 */
data class LocalGenerationParameters(
    val maxGeneratedTokens: Int,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val minP: Float,
    val repeatPenalty: Float,
    val seed: Int
) {
    init {
        require(maxGeneratedTokens > 0) {
            "maxGeneratedTokens deve essere positivo."
        }

        require(temperature.isFinite() && temperature > 0.0f) {
            "temperature deve essere finita e positiva."
        }

        require(topK > 0) {
            "topK deve essere positivo."
        }

        require(topP.isFinite() && topP > 0.0f && topP <= 1.0f) {
            "topP deve essere compreso tra 0 escluso e 1 incluso."
        }

        require(minP.isFinite() && minP >= 0.0f && minP <= 1.0f) {
            "minP deve essere compreso tra 0 e 1."
        }

        require(
            repeatPenalty.isFinite() &&
                    repeatPenalty > 0.0f
        ) {
            "repeatPenalty deve essere finito e positivo."
        }
    }
}