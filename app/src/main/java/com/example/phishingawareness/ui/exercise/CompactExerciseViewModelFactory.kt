package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import java.util.concurrent.Executor

class CompactExerciseViewModelFactory(
    private val generator:
    CompactExerciseGenerator,
    private val executor:
    Executor,
    private val generationRequest:
    GenerationRequest
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                CompactExerciseViewModel::class.java
            )
        ) {
            return CompactExerciseViewModel(
                generator = generator,
                executor = executor,
                generationRequest =
                    generationRequest
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel non supportato: ${modelClass.name}"
        )
    }
}