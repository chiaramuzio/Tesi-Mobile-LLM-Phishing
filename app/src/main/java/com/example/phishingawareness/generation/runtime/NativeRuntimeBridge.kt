package com.example.phishingawareness.generation.runtime

object NativeRuntimeBridge {

    init {
        System.loadLibrary(
            "phishingawareness_native"
        )
    }

    external fun nativeVersion(): String

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
}