package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.SampleExerciseProvider

data class QuizResult(
    val correctSelected: Int,
    val totalCorrect: Int,
    val incorrectSelected: Int
)

class ExerciseViewModel : ViewModel() {

    private val _exercise = MutableLiveData(
        SampleExerciseProvider.create()
    )

    val exercise: LiveData<Exercise>
        get() = _exercise

    private val _selectedOptionIds =
        MutableLiveData<Set<String>>(emptySet())

    val selectedOptionIds: LiveData<Set<String>>
        get() = _selectedOptionIds

    private val _quizResult =
        MutableLiveData<QuizResult?>(null)

    val quizResult: LiveData<QuizResult?>
        get() = _quizResult

    fun setOptionSelected(
        optionId: String,
        isSelected: Boolean
    ) {
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
        val currentExercise = _exercise.value ?: return
        val selectedIds = _selectedOptionIds.value.orEmpty()

        val correctOptions =
            currentExercise.quizOptions.filter { it.isCorrect }

        val correctSelected =
            correctOptions.count { it.id in selectedIds }

        val incorrectSelected =
            currentExercise.quizOptions.count {
                !it.isCorrect && it.id in selectedIds
            }

        _quizResult.value = QuizResult(
            correctSelected = correctSelected,
            totalCorrect = correctOptions.size,
            incorrectSelected = incorrectSelected
        )
    }
}