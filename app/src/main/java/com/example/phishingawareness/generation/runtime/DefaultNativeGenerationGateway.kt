package com.example.phishingawareness.generation.runtime

/**
 * Implementazione reale del gateway basata sul bridge JNI.
 */
object DefaultNativeGenerationGateway :
    NativeGenerationGateway {

    override fun generateConfiguredSequenceResult(
        prompt: String,
        addSpecial: Boolean,
        parameters: LocalGenerationParameters
    ): NativeGeneratedSequence {
        return NativeRuntimeBridge.generateConfiguredSequenceResult(
            prompt = prompt,
            addSpecial = addSpecial,
            maxGeneratedTokens =
                parameters.maxGeneratedTokens,
            temperature =
                parameters.temperature,
            topK =
                parameters.topK,
            topP =
                parameters.topP,
            minP =
                parameters.minP,
            repeatPenalty =
                parameters.repeatPenalty,
            seed =
                parameters.seed
        )
    }
}