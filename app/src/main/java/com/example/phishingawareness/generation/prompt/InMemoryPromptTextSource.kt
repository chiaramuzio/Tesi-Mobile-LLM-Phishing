package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTextReadIssueCode
import com.example.phishingawareness.domain.model.PromptTextReadResult
import com.example.phishingawareness.domain.prompt.PromptTextSource

/**
 * Sorgente di testo in memoria utilizzata per test e componenti
 * indipendenti dall'ambiente Android.
 */
class InMemoryPromptTextSource(
    contentsByPath: Map<String, String>
) : PromptTextSource {

    private val contents = contentsByPath.toMap()

    override fun read(
        assetPath: String
    ): PromptTextReadResult {
        if (assetPath.isBlank()) {
            return PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_ASSET_PATH,
                assetPath = assetPath
            )
        }

        val text = contents[assetPath]
            ?: return PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.ASSET_NOT_FOUND,
                assetPath = assetPath
            )

        if (text.isBlank()) {
            return PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_CONTENT,
                assetPath = assetPath
            )
        }

        return PromptTextReadResult.Success(
            text = text
        )
    }
}