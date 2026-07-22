package com.example.phishingawareness.generation.runtime

class JniNativeGenerationExecutor(
    private val protocolParser:
    NativeGenerationProtocolParser =
        NativeGenerationProtocolParser()
) : NativeGenerationExecutor {

    override fun generate(
        request: NativeGenerationRequest
    ): NativeGenerationResult {
        val validationFailure =
            validate(request)

        if (validationFailure != null) {
            return validationFailure
        }

        val rawResponse =
            NativeRuntimeBridge.generateGreedySequence(
                prompt = request.prompt,
                addSpecial = request.addSpecial,
                maxGeneratedTokens =
                    request.maxGeneratedTokens
            )

        return protocolParser.parse(
            rawResponse = rawResponse
        )
    }

    private fun validate(
        request: NativeGenerationRequest
    ): NativeGenerationResult.Failure? {
        if (request.prompt.isBlank()) {
            return NativeGenerationResult.Failure(
                code =
                    NativeGenerationFailureCode
                        .PROMPT_EMPTY,
                rawResponse =
                    "KOTLIN|PROMPT_EMPTY"
            )
        }

        if (
            request.maxGeneratedTokens <= 0 ||
            request.maxGeneratedTokens > 8
        ) {
            return NativeGenerationResult.Failure(
                code =
                    NativeGenerationFailureCode
                        .INVALID_MAX_GENERATED_TOKENS,
                rawResponse =
                    "KOTLIN|INVALID_MAX_GENERATED_TOKENS"
            )
        }

        return null
    }
}