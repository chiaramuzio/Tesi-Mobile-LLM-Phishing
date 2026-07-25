package com.example.phishingawareness.generation.runtime

/**
 * Astrazione sostituibile delle operazioni JNI relative
 * al ciclo di vita del modello e del context.
 */
interface NativeModelSessionGateway {

    fun loadModel(
        modelPath: String
    ): String

    fun isModelLoaded(): Boolean

    fun unloadModel(): String

    fun createContext(
        contextSize: Int
    ): String

    fun isContextReady(): Boolean

    fun contextSize(): Long

    fun freeContext(): String
}