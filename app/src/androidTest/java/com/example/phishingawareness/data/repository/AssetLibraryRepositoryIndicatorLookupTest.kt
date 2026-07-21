package com.example.phishingawareness.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetLibraryRepositoryIndicatorLookupTest {

    private val context =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext

    private val repository =
        AssetLibraryRepository(
            dataSource = LibraryAssetDataSource(context)
        )

    @Test
    fun getIndicatorById_returnsExpectedIndicator() {
        val indicator =
            repository.getIndicatorById(
                "URGENCY_PRESSURE"
            )

        assertNotNull(indicator)
        assertEquals(
            "IND_URGENCY",
            indicator?.promptId
        )
    }

    @Test
    fun getIndicatorByPromptId_returnsExpectedIndicator() {
        val indicator =
            repository.getIndicatorByPromptId(
                "IND_CREDENTIAL_REQUEST"
            )

        assertNotNull(indicator)
        assertEquals(
            "SENSITIVE_DATA_REQUEST",
            indicator?.id
        )
    }

    @Test
    fun getIndicatorById_withUnknownId_returnsNull() {
        val indicator =
            repository.getIndicatorById(
                "UNKNOWN_INDICATOR"
            )

        assertNull(indicator)
    }

    @Test
    fun getIndicatorByPromptId_withUnknownId_returnsNull() {
        val indicator =
            repository.getIndicatorByPromptId(
                "IND_UNKNOWN"
            )

        assertNull(indicator)
    }
}