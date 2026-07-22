package com.example.phishingawareness.generation.runtime

class JniNativeSamplingConfigurationValidator(
    private val rawValidator:
    NativeSamplingRawValidator =
        NativeSamplingRawValidator { configuration ->
            NativeRuntimeBridge
                .validateSamplingConfiguration(
                    maxGeneratedTokens =
                        configuration.maxGeneratedTokens,
                    temperature =
                        configuration.temperature,
                    topK =
                        configuration.topK,
                    topP =
                        configuration.topP,
                    minP =
                        configuration.minP,
                    repeatPenalty =
                        configuration.repeatPenalty,
                    seed =
                        configuration.seed
                )
        },
    private val protocolParser:
    NativeSamplingValidationProtocolParser =
        NativeSamplingValidationProtocolParser()
) : NativeSamplingConfigurationValidator {

    override fun validate(
        configuration: NativeSamplingConfiguration
    ): NativeSamplingValidationResult {
        val kotlinIssues =
            configuration.validate()

        if (kotlinIssues.isNotEmpty()) {
            return NativeSamplingValidationResult.Invalid(
                code =
                    mapKotlinIssue(
                        kotlinIssues.first()
                    ),
                rawResponse =
                    "KOTLIN|INVALID_SAMPLING_CONFIGURATION|" +
                            kotlinIssues.joinToString(
                                separator = ","
                            )
            )
        }

        val rawResponse =
            rawValidator.validate(
                configuration
            )

        return protocolParser.parse(
            rawResponse = rawResponse,
            configuration = configuration
        )
    }

    private fun mapKotlinIssue(
        issue: NativeSamplingConfigurationIssue
    ): NativeSamplingValidationFailureCode =
        when (issue) {
            NativeSamplingConfigurationIssue
                .INVALID_MAX_GENERATED_TOKENS ->
                NativeSamplingValidationFailureCode
                    .INVALID_MAX_GENERATED_TOKENS

            NativeSamplingConfigurationIssue
                .INVALID_TEMPERATURE ->
                NativeSamplingValidationFailureCode
                    .INVALID_TEMPERATURE

            NativeSamplingConfigurationIssue
                .INVALID_TOP_K ->
                NativeSamplingValidationFailureCode
                    .INVALID_TOP_K

            NativeSamplingConfigurationIssue
                .INVALID_TOP_P ->
                NativeSamplingValidationFailureCode
                    .INVALID_TOP_P

            NativeSamplingConfigurationIssue
                .INVALID_MIN_P ->
                NativeSamplingValidationFailureCode
                    .INVALID_MIN_P

            NativeSamplingConfigurationIssue
                .INVALID_REPEAT_PENALTY ->
                NativeSamplingValidationFailureCode
                    .INVALID_REPEAT_PENALTY
        }
}

fun interface NativeSamplingRawValidator {

    fun validate(
        configuration: NativeSamplingConfiguration
    ): String
}