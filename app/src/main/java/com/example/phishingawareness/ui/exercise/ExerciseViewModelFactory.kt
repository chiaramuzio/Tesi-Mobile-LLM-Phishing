package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExerciseViewModelFactory(
    private val sampleExerciseProvider: SampleExerciseProvider,
    private val scenarioId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                ExerciseViewModel::class.java
            )
        ) {
            return ExerciseViewModel(
                sampleExerciseProvider =
                    sampleExerciseProvider,
                scenarioId = scenarioId
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel non supportato: ${modelClass.name}"
        )
    }
}