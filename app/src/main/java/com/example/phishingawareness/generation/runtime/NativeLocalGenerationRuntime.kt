package com.example.phishingawareness.generation.runtime

/**
 * Adattatore applicativo del runtime basato su llama.cpp.
 *
 * Riceve un prompt già completo e restituisce il raw output tipizzato.
 */
class NativeLocalGenerationRuntime(
    private val gateway: NativeGenerationGateway =
        DefaultNativeGenerationGateway,
    private val addSpecialTokens: Boolean = true
) : LocalGenerationRuntime {

    override fun generate(
        prompt: String,
        parameters: LocalGenerationParameters
    ): NativeGeneratedSequence {
        require(prompt.isNotBlank()) {
            "Il prompt non può essere vuoto."
        }

        return gateway.generateConfiguredSequenceResult(
            prompt = prompt,
            addSpecial = addSpecialTokens,
            parameters = parameters
        )
    }
}