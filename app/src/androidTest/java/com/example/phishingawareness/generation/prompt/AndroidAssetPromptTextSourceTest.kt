package com.example.phishingawareness.generation.prompt

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.phishingawareness.domain.model.PromptTextReadIssueCode
import com.example.phishingawareness.domain.model.PromptTextReadResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidAssetPromptTextSourceTest {

    private val assetManager =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .assets

    private val source =
        AndroidAssetPromptTextSource(assetManager)

    @Test
    fun read_bankingAsset_returnsNonEmptyPrompt() {
        val result = source.read(
            "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt"
        )

        assertTrue(result is PromptTextReadResult.Success)

        val success = result as PromptTextReadResult.Success

        assertTrue(success.text.isNotBlank())
        assertTrue(
            success.text.contains(
                "Cybersecurity Awareness"
            )
        )
    }

    @Test
    fun read_accountItAsset_returnsNonEmptyPrompt() {
        val result = source.read(
            "prompts/zero_shot/ACCOUNT_IT_01_ZERO_SHOT_v3.txt"
        )

        assertTrue(result is PromptTextReadResult.Success)

        val success = result as PromptTextReadResult.Success

        assertTrue(success.text.isNotBlank())
        assertTrue(
            success.text.contains(
                "Cybersecurity Awareness"
            )
        )
    }

    @Test
    fun read_missingAsset_returnsAssetNotFound() {
        val missingPath =
            "prompts/zero_shot/not_existing.txt"

        val result = source.read(missingPath)

        assertTrue(result is PromptTextReadResult.Failure)

        val failure = result as PromptTextReadResult.Failure

        assertEquals(
            PromptTextReadIssueCode.ASSET_NOT_FOUND,
            failure.code
        )
        assertEquals(
            missingPath,
            failure.assetPath
        )
    }

    @Test
    fun read_blankPath_returnsEmptyAssetPath() {
        val result = source.read("   ")

        assertEquals(
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_ASSET_PATH,
                assetPath = "   "
            ),
            result
        )
    }
}