package com.example.phishingawareness.generation.model

import android.content.Context
import java.io.File

/**
 * Risolve il modello nella directory esterna privata dell'app:
 *
 * files/models/gemma-3-1b-it-q4_0.gguf
 *
 * Non copia, scarica o modifica il modello.
 */
class AndroidLocalModelPathProvider(
    context: Context
) : LocalModelPathProvider {

    private val applicationContext =
        context.applicationContext

    override fun resolve(): LocalModelPathResult {
        val modelsDirectory =
            applicationContext.getExternalFilesDir(
                MODELS_DIRECTORY
            )
                ?: return LocalModelPathResult.Unavailable(
                    code =
                        LocalModelPathFailureCode
                            .EXTERNAL_DIRECTORY_NOT_AVAILABLE,
                    details =
                        "La directory esterna privata dell'app " +
                                "non Ã¨ disponibile."
                )

        val modelFile =
            File(
                modelsDirectory,
                MODEL_FILE_NAME
            )

        if (!modelFile.exists()) {
            return LocalModelPathResult.Unavailable(
                code =
                    LocalModelPathFailureCode
                        .MODEL_FILE_NOT_FOUND,
                expectedPath =
                    modelFile.absolutePath
            )
        }

        if (!modelFile.isFile) {
            return LocalModelPathResult.Unavailable(
                code =
                    LocalModelPathFailureCode
                        .MODEL_PATH_NOT_A_FILE,
                expectedPath =
                    modelFile.absolutePath
            )
        }

        val actualSize =
            modelFile.length()

        if (actualSize <= 0L) {
            return LocalModelPathResult.Unavailable(
                code =
                    LocalModelPathFailureCode
                        .MODEL_FILE_EMPTY,
                expectedPath =
                    modelFile.absolutePath
            )
        }

        if (actualSize != EXPECTED_MODEL_SIZE_BYTES) {
            return LocalModelPathResult.Unavailable(
                code =
                    LocalModelPathFailureCode
                        .MODEL_FILE_SIZE_MISMATCH,
                expectedPath =
                    modelFile.absolutePath,
                details =
                    "Dimensione attesa: " +
                            "$EXPECTED_MODEL_SIZE_BYTES byte; " +
                            "dimensione rilevata: $actualSize byte."
            )
        }

        return LocalModelPathResult.Available(
            absolutePath =
                modelFile.absolutePath,
            sizeBytes =
                actualSize
        )
    }

    private companion object {
        const val MODELS_DIRECTORY =
            "models"

        const val MODEL_FILE_NAME =
            "gemma-3-1b-it-q4_0.gguf"

        const val EXPECTED_MODEL_SIZE_BYTES =
            1_003_541_152L
    }
}
