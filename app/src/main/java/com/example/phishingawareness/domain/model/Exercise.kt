package com.example.phishingawareness.domain.model

data class Exercise(
    val email: SimulatedEmail,
    val quizOptions: List<QuizOption>
) {
    init {
        require(quizOptions.size == QUIZ_OPTIONS_COUNT) {
            "Un esercizio deve contenere esattamente $QUIZ_OPTIONS_COUNT opzioni"
        }

        require(quizOptions.map { it.id }.distinct().size == quizOptions.size) {
            "Ogni opzione del quiz deve avere un identificatore univoco"
        }
    }

    companion object {
        const val QUIZ_OPTIONS_COUNT = 6
    }
}

data class SimulatedEmail(
    val senderName: String,
    val senderAddress: String,
    val subject: String,
    val body: String
)

data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)