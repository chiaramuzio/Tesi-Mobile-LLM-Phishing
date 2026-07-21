package com.example.phishingawareness.generation.prompt

import android.content.res.AssetManager
import com.example.phishingawareness.domain.model.PromptTextReadIssueCode
import com.example.phishingawareness.domain.model.PromptTextReadResult
import com.example.phishingawareness.domain.prompt.PromptTextSource
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Legge i testi dei prompt dagli asset Android.
 *
 * Il percorso ricevuto è relativo alla cartella app/src/main/assets.
 * Il contenuto viene letto in UTF-8 senza applicare trim,
 * sostituzioni o normalizzazioni.
 */
class AndroidAssetPromptTextSource(
    private val assetManager: AssetManager
) : PromptTextSource {

    override fun read(
        assetPath: String
    ): PromptTextReadResult {
        if (assetPath.isBlank()) {
            return PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.EMPTY_ASSET_PATH,
                assetPath = assetPath
            )
        }

        return try {
            val text = assetManager
                .open(assetPath)
                .reader(Charsets.UTF_8)
                .use { reader ->
                    reader.readText()
                }

            if (text.isBlank()) {
                PromptTextReadResult.Failure(
                    code = PromptTextReadIssueCode.EMPTY_CONTENT,
                    assetPath = assetPath
                )
            } else {
                PromptTextReadResult.Success(
                    text = text
                )
            }
        } catch (exception: FileNotFoundException) {
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.ASSET_NOT_FOUND,
                assetPath = assetPath,
                details = exception.message
            )
        } catch (exception: IOException) {
            PromptTextReadResult.Failure(
                code = PromptTextReadIssueCode.READ_ERROR,
                assetPath = assetPath,
                details = exception.message
            )
        }
    }
}