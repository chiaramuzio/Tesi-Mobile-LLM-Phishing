package com.example.phishingawareness.generation.runtime

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Converte il protocollo testuale restituito dal bridge JNI
 * in un risultato Kotlin tipizzato.
 *
 * Protocollo atteso:
 *
 * OK|GREEDY_SEQUENCE
 * |REQUESTED_TOKEN_COUNT|1
 * |GENERATED_TOKEN_COUNT|1
 * |EOG|0
 * |TOKEN_IDS|107
 * |OUTPUT_HEX|0a
 */
object NativeGeneratedSequenceParser {

    private const val SUCCESS_PREFIX =
        "OK|GREEDY_SEQUENCE|"

    private const val OUTPUT_HEX_MARKER =
        "|OUTPUT_HEX|"

    private const val EXPECTED_METADATA_PARTS = 10

    fun parse(
        protocol: String
    ): NativeGeneratedSequence {
        require(protocol.startsWith(SUCCESS_PREFIX)) {
            "Protocollo nativo non valido: prefisso di successo assente."
        }

        val outputMarkerIndex =
            protocol.indexOf(OUTPUT_HEX_MARKER)

        require(outputMarkerIndex >= 0) {
            "Protocollo nativo non valido: campo OUTPUT_HEX assente."
        }

        val metadata =
            protocol.substring(
                startIndex = 0,
                endIndex = outputMarkerIndex
            )

        val outputHex =
            protocol.substring(
                startIndex =
                    outputMarkerIndex +
                            OUTPUT_HEX_MARKER.length
            )

        val parts =
            metadata.split('|')

        require(parts.size == EXPECTED_METADATA_PARTS) {
            "Protocollo nativo non valido: struttura dei metadati inattesa."
        }

        require(parts[0] == "OK") {
            "Protocollo nativo non valido: stato diverso da OK."
        }

        require(parts[1] == "GREEDY_SEQUENCE") {
            "Protocollo nativo non valido: tipo di risultato inatteso."
        }

        require(parts[2] == "REQUESTED_TOKEN_COUNT") {
            "Protocollo nativo non valido: REQUESTED_TOKEN_COUNT assente."
        }

        val requestedTokenCount =
            parts[3].toPositiveInt(
                fieldName = "REQUESTED_TOKEN_COUNT"
            )

        require(parts[4] == "GENERATED_TOKEN_COUNT") {
            "Protocollo nativo non valido: GENERATED_TOKEN_COUNT assente."
        }

        val generatedTokenCount =
            parts[5].toNonNegativeInt(
                fieldName = "GENERATED_TOKEN_COUNT"
            )

        require(parts[6] == "EOG") {
            "Protocollo nativo non valido: EOG assente."
        }

        val reachedEndOfGeneration =
            when (parts[7]) {
                "0" -> false
                "1" -> true

                else -> {
                    throw IllegalArgumentException(
                        "Protocollo nativo non valido: EOG deve essere 0 oppure 1."
                    )
                }
            }

        require(parts[8] == "TOKEN_IDS") {
            "Protocollo nativo non valido: TOKEN_IDS assente."
        }

        val tokenIds =
            parseTokenIds(
                value = parts[9]
            )

        require(tokenIds.size == generatedTokenCount) {
            "Protocollo nativo non valido: il numero dei TOKEN_IDS " +
                    "non corrisponde a GENERATED_TOKEN_COUNT."
        }

        require(generatedTokenCount <= requestedTokenCount) {
            "Protocollo nativo non valido: sono stati generati più token " +
                    "di quelli richiesti."
        }

        val outputBytes =
            decodeHex(
                value = outputHex
            )

        val rawText =
            decodeUtf8(
                bytes = outputBytes
            )

        return NativeGeneratedSequence(
            requestedTokenCount = requestedTokenCount,
            generatedTokenCount = generatedTokenCount,
            reachedEndOfGeneration = reachedEndOfGeneration,
            tokenIds = tokenIds,
            rawText = rawText
        )
    }

    private fun parseTokenIds(
        value: String
    ): List<Int> {
        if (value.isBlank()) {
            return emptyList()
        }

        return value
            .split(',')
            .map { tokenValue ->
                tokenValue.toIntOrNull()
                    ?: throw IllegalArgumentException(
                        "Protocollo nativo non valido: TOKEN_IDS contiene " +
                                "un identificatore non numerico."
                    )
            }
    }

    private fun decodeHex(
        value: String
    ): ByteArray {
        require(value.length % 2 == 0) {
            "Protocollo nativo non valido: OUTPUT_HEX ha lunghezza dispari."
        }

        if (value.isEmpty()) {
            return ByteArray(0)
        }

        return ByteArray(
            size = value.length / 2
        ) { byteIndex ->
            val characterIndex =
                byteIndex * 2

            val highNibble =
                value[characterIndex].digitToIntOrNull(
                    radix = 16
                )
                    ?: throw IllegalArgumentException(
                        "Protocollo nativo non valido: OUTPUT_HEX " +
                                "contiene caratteri non esadecimali."
                    )

            val lowNibble =
                value[characterIndex + 1].digitToIntOrNull(
                    radix = 16
                )
                    ?: throw IllegalArgumentException(
                        "Protocollo nativo non valido: OUTPUT_HEX " +
                                "contiene caratteri non esadecimali."
                    )

            (
                    highNibble.shl(4) or lowNibble
                    ).toByte()
        }
    }

    private fun decodeUtf8(
        bytes: ByteArray
    ): String {
        val decoder =
            StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(
                    CodingErrorAction.REPORT
                )
                .onUnmappableCharacter(
                    CodingErrorAction.REPORT
                )

        return try {
            decoder
                .decode(
                    ByteBuffer.wrap(bytes)
                )
                .toString()
        } catch (
            exception: Exception
        ) {
            throw IllegalArgumentException(
                "Protocollo nativo non valido: OUTPUT_HEX non contiene UTF-8 valido.",
                exception
            )
        }
    }

    private fun String.toPositiveInt(
        fieldName: String
    ): Int {
        val value =
            toIntOrNull()
                ?: throw IllegalArgumentException(
                    "Protocollo nativo non valido: $fieldName non è numerico."
                )

        require(value > 0) {
            "Protocollo nativo non valido: $fieldName deve essere positivo."
        }

        return value
    }

    private fun String.toNonNegativeInt(
        fieldName: String
    ): Int {
        val value =
            toIntOrNull()
                ?: throw IllegalArgumentException(
                    "Protocollo nativo non valido: $fieldName non è numerico."
                )

        require(value >= 0) {
            "Protocollo nativo non valido: $fieldName non può essere negativo."
        }

        return value
    }
}