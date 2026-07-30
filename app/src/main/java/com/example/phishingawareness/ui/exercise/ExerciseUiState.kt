package com.example.phishingawareness.ui.exercise

import com.example.phishingawareness.domain.model.Exercise

sealed interface ExerciseUiState {

    data object Loading : ExerciseUiState

    data class Success(
        val exercise: Exercise
    ) : ExerciseUiState

    data class Error(
        val details: String
    ) : ExerciseUiState
}
