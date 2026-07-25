package com.example.phishingawareness.generation.runtime

/**
 * Implementazione reale del gateway di sessione basata
 * sulle funzioni esposte dal bridge JNI.
 *
 * Non interpreta le risposte native: questa responsabilità
 * appartiene al gestore applicativo della sessione.
 */
object DefaultNativeModelSessionGateway :
    NativeModelSessionGateway {

    override fun loadModel(
        modelPath: String
    ): String {
        return NativeRuntimeBridge.loadModel(
            modelPath = modelPath
        )
    }

    override fun isModelLoaded(): Boolean {
        return NativeRuntimeBridge.isModelLoaded()
    }

    override fun unloadModel(): String {
        return NativeRuntimeBridge.unloadModel()
    }

    override fun createContext(
        contextSize: Int
    ): String {
        return NativeRuntimeBridge.createContext(
            contextSize = contextSize
        )
    }

    override fun isContextReady(): Boolean {
        return NativeRuntimeBridge.isContextReady()
    }

    override fun contextSize(): Long {
        return NativeRuntimeBridge.contextSize()
    }

    override fun freeContext(): String {
        return NativeRuntimeBridge.freeContext()
    }
}