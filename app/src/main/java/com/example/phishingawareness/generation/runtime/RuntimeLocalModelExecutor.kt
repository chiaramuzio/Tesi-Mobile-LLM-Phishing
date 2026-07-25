package com.example.phishingawareness.generation.runtime

import com.example.phishingawareness.domain.model.LocalModelExecutionFailureCode
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor

/**
 * Adatta il contratto LocalModelExecutor al runtime locale
 * configurabile basato su llama.cpp.
 *
 * Non costruisce il prompt, non carica il modello,
 * non crea il context e non interpreta l'output JSON.
 */
class RuntimeLocalModelExecutor(
    private val runtime: LocalGenerationRuntime
) : LocalModelExecutor {

    override fun execute(
        request: LocalModelExecutionRequest
    ): LocalModelExecutionResult {
        val validationFailure = validate(request)

        if (validationFailure != null) {
            return validationFailure
        }

        val parameters =
            try {
                LocalGenerationParameters(
                    maxGeneratedTokens =
                        request.maxGeneratedTokens,
                    temperature =
                        request.temperature,
                    topK =
                        request.topK,
                    topP =
                        request.topP,
                    minP =
                        request.minP,
                    repeatPenalty =
                        request.repeatPenalty,
                    seed =
                        request.seed
                )
            } catch (
                exception: IllegalArgumentException
            ) {
                return LocalModelExecutionResult.Failure(
                    code =
                        LocalModelExecutionFailureCode
                            .INVALID_REQUEST,
                    details =
                        exception.message
                )
            }

        val generatedSequence =
            try {
                runtime.generate(
                    prompt = request.prompt,
                    parameters = parameters
                )
            } catch (
                exception: IllegalArgumentException
            ) {
                return LocalModelExecutionResult.Failure(
                    code =
                        LocalModelExecutionFailureCode
                            .INVALID_REQUEST,
                    details =
                        exception.message
                )
            } catch (
                exception: RuntimeException
            ) {
                return LocalModelExecutionResult.Failure(
                    code =
                        LocalModelExecutionFailureCode
                            .EXECUTION_FAILED,
                    details =
                        exception.message
                )
            }

        if (generatedSequence.rawText.isBlank()) {
            return LocalModelExecutionResult.Failure(
                code =
                    LocalModelExecutionFailureCode
                        .EMPTY_OUTPUT,
                details =
                    "Il runtime locale ha restituito un output vuoto."
            )
        }

        return LocalModelExecutionResult.Success(
            rawOutput =
                generatedSequence.rawText,
            metadata =
                LocalModelExecutionMetadata(
                    promptSha256 =
                        request.promptSha256,
                    seed =
                        request.seed,
                    generatedCharacterCount =
                        generatedSequence.rawText.length
                )
        )
    }

    private fun validate(
        request: LocalModelExecutionRequest
    ): LocalModelExecutionResult.Failure? {
        if (request.prompt.isBlank()) {
            return invalidRequest(
                "Il prompt non può essere vuoto."
            )
        }

        if (request.promptSha256.isBlank()) {
            return invalidRequest(
                "promptSha256 non può essere vuoto."
            )
        }

        if (request.contextSize <= 0) {
            return invalidRequest(
                "contextSize deve essere positivo."
            )
        }

        return null
    }

    private fun invalidRequest(
        details: String
    ): LocalModelExecutionResult.Failure {
        return LocalModelExecutionResult.Failure(
            code =
                LocalModelExecutionFailureCode
                    .INVALID_REQUEST,
            details = details
        )
    }
}