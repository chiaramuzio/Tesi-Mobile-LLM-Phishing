package com.example.phishingawareness.generation.runtime

object NativeRuntimeBridge {

    init {
        System.loadLibrary(
            "phishingawareness_native"
        )
    }

    external fun nativeVersion(): String
}