package com.example.phishingawareness.data.repository

import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository
import com.example.phishingawareness.domain.usecase.GenerateLocalEmailUseCase
import com.example.phishingawareness.generation.model.LocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrapResult

/**
 * Adatta la pipeline di generazione locale al contratto usato
 * dalla schermata dell'esercizio.
 *
 * Prima di ogni generazione verifica e prepara modello e context.
 * La sessione già pronta viene riutilizzata dal bootstrap.
 *
 * Non corregge, rigenera o sostituisce l'output del modello.
 */
class LocalExerciseGenerationRepository(
    private val localModelBootstrap: LocalModelBootstrap,
    private val generateLocalEmailUseCase:
    GenerateLocalEmailUseCase,
    private val generationOptions:
    LocalEmailGenerationOptions =
        LocalEmailGenerationOptions()
) : ExerciseGenerationRepository {

    override fun generateEmail(
        request: GenerationRequest
    ): GeneratedEmail {
        prepareLocalModel()

        return when (
            val result =
                generateLocalEmailUseCase(
                    request = request,
                    options = generationOptions
                )
        ) {
            is LocalEmailGenerationResult.Success ->
                result.email

            is LocalEmailGenerationResult.Failure ->
                throw IllegalStateException(
                    buildString {
                        append(
                            "Generazione locale non riuscita"
                        )
                        append(" [")
                        append(result.stage.name)
                        append("]: ")
                        append(result.details)
                    }
                )
        }
    }

    private fun prepareLocalModel() {
        when (
            val result =
                localModelBootstrap.prepare(
                    contextSize =
                        generationOptions.contextSize
                )
        ) {
            is LocalModelBootstrapResult.Ready ->
                Unit

            is LocalModelBootstrapResult.PathFailure ->
                throw IllegalStateException(
                    buildString {
                        append(
                            "Modello locale non disponibile"
                        )
                        append(" [")
                        append(result.failure.code.name)
                        append("]")

                        result.failure.expectedPath
                            ?.takeIf { it.isNotBlank() }
                            ?.let { expectedPath ->
                                append(": ")
                                append(expectedPath)
                            }

                        result.failure.details
                            ?.takeIf { it.isNotBlank() }
                            ?.let { details ->
                                append(" | ")
                                append(details)
                            }
                    }
                )

            is LocalModelBootstrapResult.SessionFailure ->
                throw IllegalStateException(
                    buildString {
                        append(
                            "Preparazione della sessione locale non riuscita"
                        )
                        append(" [")
                        append(result.failure.stage.name)
                        append("/")
                        append(result.failure.code.name)
                        append("]")

                        result.failure.details
                            ?.takeIf { it.isNotBlank() }
                            ?.let { details ->
                                append(": ")
                                append(details)
                            }
                    }
                )
        }
    }
}