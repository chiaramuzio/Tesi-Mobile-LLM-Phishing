package com.example.phishingawareness.ui.exercise

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult

sealed class CompactExerciseUiState {

    object Idle : CompactExerciseUiState()

    object Loading : CompactExerciseUiState()

    data class Success(
        val result:
        CompactExerciseGenerationResult.Success
    ) : CompactExerciseUiState()

    data class Error(
        val cause: CompactExerciseUiError
    ) : CompactExerciseUiState()
}

sealed class CompactExerciseUiError {

    data class Generation(
        val failure:
        CompactExerciseGenerationResult.Failure
    ) : CompactExerciseUiError()

    data class Unexpected(
        val details: String
    ) : CompactExerciseUiError()
}