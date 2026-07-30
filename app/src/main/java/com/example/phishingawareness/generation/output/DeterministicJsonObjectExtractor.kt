package com.example.phishingawareness.generation.output

/**
 * Estrae un singolo oggetto JSON dal raw output del modello.
 *
 * Non corregge, completa o modifica il JSON:
 * restituisce esattamente il testo compreso tra la prima
 * parentesi graffa aperta e l'ultima parentesi graffa chiusa.
 *
 * L'eventuale testo esterno, per esempio un wrapper Qwen
 * <think>...</think>, viene escluso.
 */
class DeterministicJsonObjectExtractor {

    fun extract(
        rawOutput: String
    ): String? {
        val normalizedOutput =
            rawOutput.trim()

        if (normalizedOutput.isEmpty()) {
            return null
        }

        val jsonStart =
            normalizedOutput.indexOf('{')

        val jsonEnd =
            normalizedOutput.lastIndexOf('}')

        if (
            jsonStart < 0 ||
            jsonEnd < jsonStart
        ) {
            return null
        }

        return normalizedOutput.substring(
            startIndex = jsonStart,
            endIndex = jsonEnd + 1
        )
    }
}