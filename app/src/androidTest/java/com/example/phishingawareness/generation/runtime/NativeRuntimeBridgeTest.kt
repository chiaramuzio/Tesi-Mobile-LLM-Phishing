package com.example.phishingawareness.generation.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeRuntimeBridgeTest {

    @Test
    fun nativeVersion_nativeLibraryLoaded_returnsExpectedVersion() {
        assertEquals(
            "phishingawareness-native-1",
            NativeRuntimeBridge.nativeVersion()
        )
    }
}