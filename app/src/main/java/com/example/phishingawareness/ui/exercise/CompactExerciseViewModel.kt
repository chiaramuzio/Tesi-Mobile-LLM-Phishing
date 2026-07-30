package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class CompactExerciseViewModel(
    generator: CompactExerciseGenerator,
    executor: Executor,
    private val generationRequest:
    GenerationRequest
) : ViewModel() {

    private val acceptingUpdates =
        AtomicBoolean(true)

    private val _uiState =
        MutableLiveData<CompactExerciseUiState>(
            CompactExerciseUiState.Idle
        )

    val uiState: LiveData<CompactExerciseUiState>
        get() = _uiState

    private val runner =
        CompactExerciseAsyncRunner(
            generator = generator,
            executor = executor,
            stateObserver = { state ->
                if (acceptingUpdates.get()) {
                    _uiState.postValue(state)
                }
            }
        )

    fun startGeneration(): Boolean {
        return runner.start(
            request = generationRequest
        )
    }

    fun isGenerationRunning(): Boolean {
        return runner.isRunning()
    }

    override fun onCleared() {
        acceptingUpdates.set(false)
        super.onCleared()
    }
}