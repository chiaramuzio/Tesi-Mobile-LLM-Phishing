package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTextReadIssueCode
import com.example.phishingawareness.domain.model.PromptTextReadResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPromptTextSourceTest {

    @Test
    fun read_withAvailablePath_returnsExactText() {
        val path =
            "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt"

        val expectedText =
            "Prima riga\n\nSeconda riga con accento: credibilità"

        val source = InMemoryPromptTextSource(
            contentsByPath = mapOf(
                path to expectedText
            )
        )

        val result = source.read(path)

        assertEquals(
            PromptTextReadResult.Success(expectedText),
            result
        )
    }

    @Test
    fun read_withMissingPath_returnsAssetNotFound() {
        val missingPath =
            "prompts/zero_shot/not_existing.txt"

        val source = InMemoryPromptTextSource(
            contentsByPath = emptyMap()
        )

        val result = source.read(missingPath)

        assertEquals(
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.ASSET_NOT_FOUND,
                assetPath = missingPath
            ),
            result
        )
    }

    @Test
    fun read_withBlankPath_returnsEmptyAssetPath() {
        val source = InMemoryPromptTextSource(
            contentsByPath = emptyMap()
        )

        val result = source.read("   ")

        assertEquals(
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_ASSET_PATH,
                assetPath = "   "
            ),
            result
        )
    }

    @Test
    fun read_withBlankContent_returnsEmptyContent() {
        val path =
            "prompts/zero_shot/empty.txt"

        val source = InMemoryPromptTextSource(
            contentsByPath = mapOf(
                path to "\n   \n"
            )
        )

        val result = source.read(path)

        assertEquals(
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_CONTENT,
                assetPath = path
            ),
            result
        )
    }

    @Test
    fun read_doesNotNormalizeReturnedText() {
        val path =
            "prompts/zero_shot/test.txt"

        val originalText =
            "  Prima riga  \r\n\r\nSeconda riga\n"

        val source = InMemoryPromptTextSource(
            contentsByPath = mapOf(
                path to originalText
            )
        )

        val result = source.read(path)

        assertTrue(result is PromptTextReadResult.Success)

        val success = result as PromptTextReadResult.Success

        assertEquals(originalText, success.text)
    }
}