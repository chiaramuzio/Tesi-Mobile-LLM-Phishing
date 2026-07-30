package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.modelruntime.CompactRuntimeLifecycle
import com.example.phishingawareness.domain.modelruntime.CompactRuntimePreparationResult

class PreparedCompactExerciseGenerator(
    private val runtimeLifecycle:
    CompactRuntimeLifecycle,
    private val delegate:
    CompactExerciseGenerator,
    private val contextSize: Int = DEFAULT_CONTEXT_SIZE
) : CompactExerciseGenerator {

    init {
        require(contextSize > 0) {
            "contextSize deve essere maggiore di zero."
        }
    }

    override fun generate(
        request: GenerationRequest
    ): CompactExerciseGenerationResult {
        val preparationResult =
            try {
                runtimeLifecycle.prepare(
                    contextSize = contextSize
                )
            } catch (
                exception: RuntimeException
            ) {
                return lifecycleFailure(
                    prefix =
                        "Preparazione del runtime fallita",
                    exception = exception
                )
            }

        if (
            preparationResult is
                    CompactRuntimePreparationResult.Failure
        ) {
            return CompactExerciseGenerationResult
                .Failure
                .RuntimeLifecycle(
                    details =
                        preparationResult.details
                            .ifBlank {
                                "Preparazione del runtime fallita."
                            }
                )
        }

        return try {
            delegate.generate(
                request = request
            )
        } catch (
            exception: RuntimeException
        ) {
            lifecycleFailure(
                prefix =
                    "Generazione compatta fallita",
                exception = exception
            )
        } finally {
            try {
                runtimeLifecycle.release()
            } catch (
                ignored: RuntimeException
            ) {
                /*
                 * Il risultato della generazione non viene sostituito
                 * da un errore di rilascio. Il rilascio concreto dovrà
                 * registrare autonomamente la diagnostica.
                 */
            }
        }
    }

    private fun lifecycleFailure(
        prefix: String,
        exception: RuntimeException
    ): CompactExerciseGenerationResult.Failure {
        val exceptionDetails =
            exception.message
                ?.takeIf { message ->
                    message.isNotBlank()
                }
                ?: exception.javaClass.simpleName

        return CompactExerciseGenerationResult
            .Failure
            .RuntimeLifecycle(
                details =
                    "$prefix: $exceptionDetails"
            )
    }

    private companion object {

        const val DEFAULT_CONTEXT_SIZE = 8192
    }
}