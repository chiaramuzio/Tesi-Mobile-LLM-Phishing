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
}