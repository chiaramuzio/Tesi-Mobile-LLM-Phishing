package com.example.phishingawareness.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryIndicatorPromptIdTest {

    private val context =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    private val dataSource =
        LibraryAssetDataSource(context)

    @Test
    fun loadManifest_returnsUpdatedLibraryVersion() {
        val manifest = dataSource.loadManifest()

        assertEquals(
            "phishing-awareness-library",
            manifest.libraryId
        )
        assertEquals("0.3.0", manifest.version)
        assertEquals(2, manifest.schemaVersion)
    }

    @Test
    fun loadIndicators_returnsUniqueInternalAndPromptIds() {
        val indicators = dataSource.loadIndicators()

        assertTrue(indicators.isNotEmpty())

        assertEquals(
            indicators.size,
            indicators.map { indicator -> indicator.id }
                .distinct()
                .size
        )

        assertEquals(
            indicators.size,
            indicators.map { indicator -> indicator.promptId }
                .distinct()
                .size
        )

        assertTrue(
            indicators.all { indicator ->
                indicator.id.isNotBlank() &&
                        indicator.promptId.isNotBlank()
            }
        )
    }

    @Test
    fun loadIndicators_containsScientificBaselineMappings() {
        val indicators = dataSource.loadIndicators()

        val mappings =
            indicators.associate { indicator ->
                indicator.id to indicator.promptId
            }

        assertEquals(
            "IND_URGENCY",
            mappings["URGENCY_PRESSURE"]
        )

        assertEquals(
            "IND_SUSPICIOUS_LINK",
            mappings["SUSPICIOUS_LINK"]
        )

        assertEquals(
            "IND_CREDENTIAL_REQUEST",
            mappings["SENSITIVE_DATA_REQUEST"]
        )
    }
}