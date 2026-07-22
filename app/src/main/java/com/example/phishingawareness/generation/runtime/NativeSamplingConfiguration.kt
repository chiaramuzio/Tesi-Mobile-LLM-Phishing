package com.example.phishingawareness.generation.runtime

data class NativeSamplingConfiguration(
    val maxGeneratedTokens: Int,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val minP: Float,
    val repeatPenalty: Float,
    val seed: Int
) {

    fun validate(): List<NativeSamplingConfigurationIssue> =
        buildList {
            if (maxGeneratedTokens <= 0) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_MAX_GENERATED_TOKENS
                )
            }

            if (!temperature.isFinite() ||
                temperature <= 0.0f
            ) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_TEMPERATURE
                )
            }

            if (topK <= 0) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_TOP_K
                )
            }

            if (!topP.isFinite() ||
                topP <= 0.0f ||
                topP > 1.0f
            ) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_TOP_P
                )
            }

            if (!minP.isFinite() ||
                minP < 0.0f ||
                minP > 1.0f
            ) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_MIN_P
                )
            }

            if (!repeatPenalty.isFinite() ||
                repeatPenalty <= 0.0f
            ) {
                add(
                    NativeSamplingConfigurationIssue
                        .INVALID_REPEAT_PENALTY
                )
            }
        }

    companion object {

        val ThesisDefault =
            NativeSamplingConfiguration(
                maxGeneratedTokens = 1_200,
                temperature = 0.4f,
                topK = 40,
                topP = 0.90f,
                minP = 0.05f,
                repeatPenalty = 1.05f,
                seed = 101
            )
    }
}

enum class NativeSamplingConfigurationIssue {
    INVALID_MAX_GENERATED_TOKENS,
    INVALID_TEMPERATURE,
    INVALID_TOP_K,
    INVALID_TOP_P,
    INVALID_MIN_P,
    INVALID_REPEAT_PENALTY
}