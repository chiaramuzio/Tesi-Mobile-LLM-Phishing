package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.GenerateExerciseUseCase

class ExerciseViewModelFactory(
    private val generateExerciseUseCase:
    GenerateExerciseUseCase,
    private val generationRequest:
    GenerationRequest
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
                generateExerciseUseCase =
                    generateExerciseUseCase,
                generationRequest =
                    generationRequest
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel non supportato: ${modelClass.name}"
        )
    }
}