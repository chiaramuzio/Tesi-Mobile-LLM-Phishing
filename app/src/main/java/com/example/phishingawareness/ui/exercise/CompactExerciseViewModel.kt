package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phishingawareness.domain.model.Exercise
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

    private val _exercise =
        MutableLiveData<Exercise?>(null)

    val exercise: LiveData<Exercise?>
        get() = _exercise

    private val _selectedOptionIds =
        MutableLiveData<Set<String>>(emptySet())

    val selectedOptionIds: LiveData<Set<String>>
        get() = _selectedOptionIds

    private val _quizResult =
        MutableLiveData<QuizResult?>(null)

    val quizResult: LiveData<QuizResult?>
        get() = _quizResult

    private val runner =
        CompactExerciseAsyncRunner(
            generator = generator,
            executor = executor,
            stateObserver = ::handleRunnerState
        )

    fun startGeneration(): Boolean {
        return runner.start(
            request = generationRequest
        )
    }

    fun isGenerationRunning(): Boolean {
        return runner.isRunning()
    }

    fun setOptionSelected(
        optionId: String,
        isSelected: Boolean
    ) {
        val currentExercise =
            _exercise.value ?: return

        val validOptionIds =
            currentExercise.quizOptions
                .map { option -> option.id }
                .toSet()

        if (optionId !in validOptionIds) {
            return
        }

        val currentSelections =
            _selectedOptionIds.value.orEmpty()

        _selectedOptionIds.value =
            if (isSelected) {
                currentSelections + optionId
            } else {
                currentSelections - optionId
            }

        _quizResult.value = null
    }

    fun submitQuiz() {
        val currentExercise =
            _exercise.value ?: return

        val selectedIds =
            _selectedOptionIds.value.orEmpty()

        val correctOptions =
            currentExercise.quizOptions
                .filter { option ->
                    option.isCorrect
                }

        val correctSelected =
            correctOptions.count { option ->
                option.id in selectedIds
            }

        val incorrectSelected =
            currentExercise.quizOptions.count { option ->
                !option.isCorrect &&
                        option.id in selectedIds
            }

        _quizResult.value =
            QuizResult(
                correctSelected =
                    correctSelected,
                totalCorrect =
                    correctOptions.size,
                incorrectSelected =
                    incorrectSelected
            )
    }

    private fun handleRunnerState(
        state: CompactExerciseUiState
    ) {
        if (!acceptingUpdates.get()) {
            return
        }

        when (state) {
            CompactExerciseUiState.Idle ->
                Unit

            CompactExerciseUiState.Loading -> {
                _exercise.postValue(null)
                _selectedOptionIds.postValue(
                    emptySet()
                )
                _quizResult.postValue(null)
            }

            is CompactExerciseUiState.Success -> {
                _exercise.postValue(
                    state.result.exercise
                )

                _selectedOptionIds.postValue(
                    emptySet()
                )

                _quizResult.postValue(null)
            }

            is CompactExerciseUiState.Error ->
                Unit
        }

        _uiState.postValue(state)
    }

    override fun onCleared() {
        acceptingUpdates.set(false)
        super.onCleared()
    }
}