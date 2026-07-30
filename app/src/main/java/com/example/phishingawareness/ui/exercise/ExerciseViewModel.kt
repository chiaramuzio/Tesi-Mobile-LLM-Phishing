package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.GenerateExerciseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuizResult(
    val correctSelected: Int,
    val totalCorrect: Int,
    val incorrectSelected: Int
)

class ExerciseViewModel(
    private val generateExerciseUseCase: GenerateExerciseUseCase,
    private val generationRequest: GenerationRequest
) : ViewModel() {

    private val _uiState =
        MutableLiveData<ExerciseUiState>(
            ExerciseUiState.Loading
        )

    val uiState: LiveData<ExerciseUiState>
        get() = _uiState

    private val _selectedOptionIds =
        MutableLiveData<Set<String>>(emptySet())

    val selectedOptionIds: LiveData<Set<String>>
        get() = _selectedOptionIds

    private val _quizResult =
        MutableLiveData<QuizResult?>(null)

    val quizResult: LiveData<QuizResult?>
        get() = _quizResult

    init {
        generateExercise()
    }

    private fun generateExercise() {
        _uiState.value = ExerciseUiState.Loading

        viewModelScope.launch {
            val result =
                runCatching {
                    withContext(Dispatchers.IO) {
                        generateExerciseUseCase(
                            request = generationRequest
                        )
                    }
                }

            _uiState.value =
                result.fold(
                    onSuccess = { exercise ->
                        ExerciseUiState.Success(
                            exercise = exercise
                        )
                    },
                    onFailure = { exception ->
                        ExerciseUiState.Error(
                            details =
                                exception.message
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "Generazione dell'esercizio non riuscita."
                        )
                    }
                )
        }
    }

    fun setOptionSelected(
        optionId: String,
        isSelected: Boolean
    ) {
        if (_uiState.value !is ExerciseUiState.Success) {
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
            (_uiState.value as? ExerciseUiState.Success)
                ?.exercise
                ?: return

        val selectedIds =
            _selectedOptionIds.value.orEmpty()

        val correctOptions =
            currentExercise.quizOptions.filter {
                it.isCorrect
            }

        val correctSelected =
            correctOptions.count {
                it.id in selectedIds
            }

        val incorrectSelected =
            currentExercise.quizOptions.count {
                !it.isCorrect &&
                        it.id in selectedIds
            }

        _quizResult.value =
            QuizResult(
                correctSelected = correctSelected,
                totalCorrect = correctOptions.size,
                incorrectSelected = incorrectSelected
            )
    }
}
