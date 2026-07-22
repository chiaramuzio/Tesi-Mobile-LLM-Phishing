package com.example.phishingawareness.generation.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeRuntimeBridgeTest {

    @Test
    fun nativeVersion_llamaCppLinked_returnsExpectedVersion() {
        assertEquals(
            "phishingawareness-native-2-llama-b9947",
            NativeRuntimeBridge.nativeVersion()
        )
    }

    @Test
    fun llamaMaxDevices_llamaCppLinked_returnsPositiveValue() {
        val maxDevices =
            NativeRuntimeBridge.llamaMaxDevices()

        assertTrue(
            maxDevices > 0
        )
    }

    @Test
    fun llamaSupportsMmap_llamaCppLinked_canBeCalled() {
        NativeRuntimeBridge.llamaSupportsMmap()
    }
}