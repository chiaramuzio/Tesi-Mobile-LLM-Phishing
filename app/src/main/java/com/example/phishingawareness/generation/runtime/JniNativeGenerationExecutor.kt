package com.example.phishingawareness.generation.runtime

class JniNativeGenerationExecutor(
    private val nativeSequenceGenerator:
    NativeSequenceGenerator =
        NativeSequenceGenerator { prompt, addSpecial, maxGeneratedTokens ->
            NativeRuntimeBridge.generateGreedySequence(
                prompt = prompt,
                addSpecial = addSpecial,
                maxGeneratedTokens = maxGeneratedTokens
            )
        },
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
            nativeSequenceGenerator.generate(
                prompt = request.prompt,
                addSpecial = request.addSpecial,
                maxGeneratedTokens =
                    request.sampling.maxGeneratedTokens
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

        val samplingIssues =
            request.sampling.validate()

        if (samplingIssues.isNotEmpty()) {
            return NativeGenerationResult.Failure(
                code =
                    NativeGenerationFailureCode
                        .INVALID_SAMPLING_CONFIGURATION,
                rawResponse =
                    "KOTLIN|INVALID_SAMPLING_CONFIGURATION|" +
                            samplingIssues.joinToString(
                                separator = ","
                            )
            )
        }

        if (
            request.sampling.maxGeneratedTokens >
            GREEDY_PROBE_MAX_GENERATED_TOKENS
        ) {
            return NativeGenerationResult.Failure(
                code =
                    NativeGenerationFailureCode
                        .INVALID_MAX_GENERATED_TOKENS,
                rawResponse =
                    "KOTLIN|GREEDY_PROBE_LIMIT_EXCEEDED"
            )
        }

        return null
    }

    private companion object {
        const val GREEDY_PROBE_MAX_GENERATED_TOKENS =
            8
    }
}

fun interface NativeSequenceGenerator {

    fun generate(
        prompt: String,
        addSpecial: Boolean,
        maxGeneratedTokens: Int
    ): String
}