package com.example.phishingawareness.generation.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeSamplingConfigurationTest {

    @Test
    fun thesisDefault_containsApprovedParameters() {
        val configuration =
            NativeSamplingConfiguration.ThesisDefault

        assertEquals(
            1_200,
            configuration.maxGeneratedTokens
        )

        assertEquals(
            0.4f,
            configuration.temperature
        )

        assertEquals(
            40,
            configuration.topK
        )

        assertEquals(
            0.90f,
            configuration.topP
        )

        assertEquals(
            0.05f,
            configuration.minP
        )

        assertEquals(
            1.05f,
            configuration.repeatPenalty
        )

        assertEquals(
            101,
            configuration.seed
        )
    }

    @Test
    fun validate_thesisDefault_returnsNoIssues() {
        assertTrue(
            NativeSamplingConfiguration
                .ThesisDefault
                .validate()
                .isEmpty()
        )
    }

    @Test
    fun validate_invalidValues_returnsAllIssues() {
        val configuration =
            NativeSamplingConfiguration(
                maxGeneratedTokens = 0,
                temperature = 0.0f,
                topK = 0,
                topP = 1.1f,
                minP = -0.1f,
                repeatPenalty = 0.0f,
                seed = 101
            )

        assertEquals(
            listOf(
                NativeSamplingConfigurationIssue
                    .INVALID_MAX_GENERATED_TOKENS,
                NativeSamplingConfigurationIssue
                    .INVALID_TEMPERATURE,
                NativeSamplingConfigurationIssue
                    .INVALID_TOP_K,
                NativeSamplingConfigurationIssue
                    .INVALID_TOP_P,
                NativeSamplingConfigurationIssue
                    .INVALID_MIN_P,
                NativeSamplingConfigurationIssue
                    .INVALID_REPEAT_PENALTY
            ),
            configuration.validate()
        )
    }

    @Test
    fun validate_nonFiniteValues_returnsIssues() {
        val configuration =
            NativeSamplingConfiguration
                .ThesisDefault
                .copy(
                    temperature = Float.NaN,
                    topP = Float.POSITIVE_INFINITY,
                    minP = Float.NaN,
                    repeatPenalty =
                        Float.NEGATIVE_INFINITY
                )

        assertEquals(
            listOf(
                NativeSamplingConfigurationIssue
                    .INVALID_TEMPERATURE,
                NativeSamplingConfigurationIssue
                    .INVALID_TOP_P,
                NativeSamplingConfigurationIssue
                    .INVALID_MIN_P,
                NativeSamplingConfigurationIssue
                    .INVALID_REPEAT_PENALTY
            ),
            configuration.validate()
        )
    }
}