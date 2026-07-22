package com.example.phishingawareness.generation.runtime

class NativeGenerationProtocolParser {

    fun parse(
        rawResponse: String
    ): NativeGenerationResult {
        if (rawResponse.startsWith(ERROR_PREFIX)) {
            return parseFailure(rawResponse)
        }

        if (!rawResponse.startsWith(SUCCESS_PREFIX)) {
            return malformed(rawResponse)
        }

        return parseSuccess(rawResponse)
            ?: malformed(rawResponse)
    }

    private fun parseSuccess(
        rawResponse: String
    ): NativeGenerationResult.Success? {
        val requestedTokenCount =
            rawResponse
                .valueBetween(
                    startMarker = REQUESTED_COUNT_MARKER,
                    endMarker = GENERATED_COUNT_MARKER
                )
                ?.toIntOrNull()
                ?: return null

        val generatedTokenCount =
            rawResponse
                .valueBetween(
                    startMarker = GENERATED_COUNT_MARKER,
                    endMarker = EOG_MARKER
                )
                ?.toIntOrNull()
                ?: return null

        val eogValue =
            rawResponse.valueBetween(
                startMarker = EOG_MARKER,
                endMarker = TOKEN_IDS_MARKER
            ) ?: return null

        val reachedEndOfGeneration =
            when (eogValue) {
                "0" -> false
                "1" -> true
                else -> return null
            }

        val tokenIdsText =
            rawResponse.valueBetween(
                startMarker = TOKEN_IDS_MARKER,
                endMarker = OUTPUT_HEX_MARKER
            ) ?: return null

        val tokenIds =
            tokenIdsText
                .split(",")
                .map { tokenId ->
                    tokenId.toIntOrNull()
                        ?: return null
                }

        val outputHex =
            rawResponse.substringAfter(
                delimiter = OUTPUT_HEX_MARKER,
                missingDelimiterValue = ""
            )

        val outputBytes =
            outputHex.hexToByteArrayOrNull()
                ?: return null

        if (requestedTokenCount <= 0) {
            return null
        }

        if (generatedTokenCount !in 1..requestedTokenCount) {
            return null
        }

        if (tokenIds.size != generatedTokenCount) {
            return null
        }

        if (tokenIds.any { it < 0 }) {
            return null
        }

        return NativeGenerationResult.Success(
            requestedTokenCount = requestedTokenCount,
            generatedTokenCount = generatedTokenCount,
            reachedEndOfGeneration = reachedEndOfGeneration,
            tokenIds = tokenIds,
            outputBytes = outputBytes
        )
    }

    private fun parseFailure(
        rawResponse: String
    ): NativeGenerationResult.Failure {
        val nativeCode =
            rawResponse.substringAfter(
                delimiter = ERROR_PREFIX
            )

        val failureCode =
            when (nativeCode) {
                "PROMPT_NULL" ->
                    NativeGenerationFailureCode.PROMPT_NULL

                "PROMPT_EMPTY" ->
                    NativeGenerationFailureCode.PROMPT_EMPTY

                "INVALID_MAX_GENERATED_TOKENS" ->
                    NativeGenerationFailureCode.INVALID_MAX_GENERATED_TOKENS

                "MODEL_NOT_LOADED" ->
                    NativeGenerationFailureCode.MODEL_NOT_LOADED

                "CONTEXT_NOT_CREATED" ->
                    NativeGenerationFailureCode.CONTEXT_NOT_CREATED

                "TOKENIZATION_FAILED" ->
                    NativeGenerationFailureCode.TOKENIZATION_FAILED

                "CONTEXT_SIZE_EXCEEDED" ->
                    NativeGenerationFailureCode.CONTEXT_SIZE_EXCEEDED

                "PROMPT_DECODE_FAILED" ->
                    NativeGenerationFailureCode.PROMPT_DECODE_FAILED

                "SAMPLER_CREATION_FAILED" ->
                    NativeGenerationFailureCode.SAMPLER_CREATION_FAILED

                "TOKEN_PIECE_FAILED" ->
                    NativeGenerationFailureCode.TOKEN_PIECE_FAILED

                "GENERATED_TOKEN_DECODE_FAILED" ->
                    NativeGenerationFailureCode.GENERATED_TOKEN_DECODE_FAILED

                else ->
                    NativeGenerationFailureCode.UNKNOWN_NATIVE_ERROR
            }

        return NativeGenerationResult.Failure(
            code = failureCode,
            rawResponse = rawResponse
        )
    }

    private fun malformed(
        rawResponse: String
    ): NativeGenerationResult.Failure =
        NativeGenerationResult.Failure(
            code =
                NativeGenerationFailureCode
                    .MALFORMED_NATIVE_RESPONSE,
            rawResponse = rawResponse
        )

    private fun String.valueBetween(
        startMarker: String,
        endMarker: String
    ): String? {
        val startIndex = indexOf(startMarker)

        if (startIndex < 0) {
            return null
        }

        val valueStart =
            startIndex + startMarker.length

        val endIndex =
            indexOf(
                string = endMarker,
                startIndex = valueStart
            )

        if (endIndex < valueStart) {
            return null
        }

        return substring(
            startIndex = valueStart,
            endIndex = endIndex
        )
    }

    private fun String.hexToByteArrayOrNull(): ByteArray? {
        if (length % 2 != 0) {
            return null
        }

        return ByteArray(length / 2) { index ->
            substring(
                startIndex = index * 2,
                endIndex = index * 2 + 2
            )
                .toIntOrNull(radix = 16)
                ?.toByte()
                ?: return null
        }
    }

    private companion object {
        const val ERROR_PREFIX =
            "ERROR|"

        const val SUCCESS_PREFIX =
            "OK|GREEDY_SEQUENCE|"

        const val REQUESTED_COUNT_MARKER =
            "|REQUESTED_TOKEN_COUNT|"

        const val GENERATED_COUNT_MARKER =
            "|GENERATED_TOKEN_COUNT|"

        const val EOG_MARKER =
            "|EOG|"

        const val TOKEN_IDS_MARKER =
            "|TOKEN_IDS|"

        const val OUTPUT_HEX_MARKER =
            "|OUTPUT_HEX|"
    }
}