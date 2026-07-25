package com.example.phishingawareness.generation.runtime

object NativeRuntimeBridge {

    init {
        System.loadLibrary(
            "phishingawareness_native"
        )
    }

    external fun nativeVersion(): String

    external fun systemInfo(): String

    external fun contextRuntimeInfo(): String

    external fun llamaSupportsMmap(): Boolean

    external fun llamaMaxDevices(): Long

    external fun loadModelProbe(
        modelPath: String
    ): String

    external fun loadModel(
        modelPath: String
    ): String

    external fun isModelLoaded(): Boolean

    external fun unloadModel(): String

    external fun createContext(
        contextSize: Int
    ): String

    external fun isContextReady(): Boolean

    external fun contextSize(): Long

    external fun freeContext(): String

    external fun tokenizePrompt(
        prompt: String,
        addSpecial: Boolean
    ): String

    external fun decodePromptProbe(
        prompt: String,
        addSpecial: Boolean
    ): String

    external fun inspectChatPrompt(
        prompt: String,
        addSpecial: Boolean
    ): String

    external fun generateFirstTokenGreedy(
        prompt: String,
        addSpecial: Boolean
    ): String

    external fun generateGreedySequence(
        prompt: String,
        addSpecial: Boolean,
        maxGeneratedTokens: Int
    ): String

    external fun generateConfiguredSequence(
        prompt: String,
        addSpecial: Boolean,
        maxGeneratedTokens: Int,
        temperature: Float,
        topK: Int,
        topP: Float,
        minP: Float,
        repeatPenalty: Float,
        seed: Int
    ): String

    /**
     * Esegue una generazione configurabile e converte il protocollo JNI
     * nel risultato Kotlin tipizzato.
     *
     * Il metodo generateConfiguredSequence() resta disponibile per
     * diagnostica, logging e riproducibilità del protocollo originale.
     */
    fun generateConfiguredSequenceResult(
        prompt: String,
        addSpecial: Boolean,
        maxGeneratedTokens: Int,
        temperature: Float,
        topK: Int,
        topP: Float,
        minP: Float,
        repeatPenalty: Float,
        seed: Int
    ): NativeGeneratedSequence {
        val nativeProtocol =
            generateConfiguredSequence(
                prompt = prompt,
                addSpecial = addSpecial,
                maxGeneratedTokens = maxGeneratedTokens,
                temperature = temperature,
                topK = topK,
                topP = topP,
                minP = minP,
                repeatPenalty = repeatPenalty,
                seed = seed
            )

        return NativeGeneratedSequenceParser.parse(
            protocol = nativeProtocol
        )
    }
    external fun validateSamplingConfiguration(
        maxGeneratedTokens: Int,
        temperature: Float,
        topK: Int,
        topP: Float,
        minP: Float,
        repeatPenalty: Float,
        seed: Int
    ): String

    external fun probeSamplingChain(
        maxGeneratedTokens: Int,
        temperature: Float,
        topK: Int,
        topP: Float,
        minP: Float,
        repeatPenalty: Float,
        seed: Int
    ): String
}