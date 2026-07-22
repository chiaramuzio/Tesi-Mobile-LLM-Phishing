package com.example.phishingawareness.generation.runtime

class NativeSamplingValidationProtocolParser {

    fun parse(
        rawResponse: String,
        configuration: NativeSamplingConfiguration
    ): NativeSamplingValidationResult =
        when {
            rawResponse.startsWith(
                VALID_RESPONSE_PREFIX
            ) ->
                NativeSamplingValidationResult.Valid(
                    configuration = configuration,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_MAX_GENERATED_TOKENS" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_MAX_GENERATED_TOKENS,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_TEMPERATURE" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_TEMPERATURE,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_TOP_K" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_TOP_K,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_TOP_P" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_TOP_P,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_MIN_P" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_MIN_P,
                    rawResponse = rawResponse
                )

            rawResponse ==
                    "ERROR|INVALID_REPEAT_PENALTY" ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .INVALID_REPEAT_PENALTY,
                    rawResponse = rawResponse
                )

            else ->
                invalid(
                    code =
                        NativeSamplingValidationFailureCode
                            .MALFORMED_NATIVE_RESPONSE,
                    rawResponse = rawResponse
                )
        }

    private fun invalid(
        code: NativeSamplingValidationFailureCode,
        rawResponse: String
    ): NativeSamplingValidationResult.Invalid =
        NativeSamplingValidationResult.Invalid(
            code = code,
            rawResponse = rawResponse
        )

    private companion object {
        const val VALID_RESPONSE_PREFIX =
            "OK|SAMPLING_CONFIGURATION|"
    }
}