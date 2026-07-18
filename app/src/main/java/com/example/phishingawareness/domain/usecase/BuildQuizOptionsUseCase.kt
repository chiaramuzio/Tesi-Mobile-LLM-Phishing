package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.data.repository.LibraryRepository
import com.example.phishingawareness.domain.model.QuizOption

class BuildQuizOptionsUseCase(
    private val libraryRepository: LibraryRepository
) {

    operator fun invoke(
        scenarioId: String,
        presentIndicatorIds: Set<String>
    ): List<QuizOption> {
        val correctOptions =
            libraryRepository
                .getIndicatorsForScenario(scenarioId)
                .filter { indicator ->
                    indicator.id in presentIndicatorIds
                }
                .map { indicator ->
                    QuizOption(
                        id = indicator.id,
                        text = indicator.displayName,
                        isCorrect = true
                    )
                }

        require(correctOptions.isNotEmpty()) {
            "Non sono presenti indicatori osservabili per il quiz."
        }

        require(correctOptions.size <= QUIZ_OPTIONS_COUNT) {
            "Sono presenti più di $QUIZ_OPTIONS_COUNT indicatori corretti."
        }

        val requiredDistractors =
            QUIZ_OPTIONS_COUNT - correctOptions.size

        val distractorOptions =
            libraryRepository
                .getDistractorsForScenario(scenarioId)
                .take(requiredDistractors)
                .map { distractor ->
                    QuizOption(
                        id = distractor.id,
                        text = distractor.displayName,
                        isCorrect = false
                    )
                }

        require(distractorOptions.size == requiredDistractors) {
            "Distrattori insufficienti per costruire il quiz."
        }

        return interleaveOptions(
            correctOptions = correctOptions,
            distractorOptions = distractorOptions
        )
    }

    private fun interleaveOptions(
        correctOptions: List<QuizOption>,
        distractorOptions: List<QuizOption>
    ): List<QuizOption> {
        val result = mutableListOf<QuizOption>()
        val maxSize =
            maxOf(
                correctOptions.size,
                distractorOptions.size
            )

        for (index in 0 until maxSize) {
            correctOptions
                .getOrNull(index)
                ?.let(result::add)

            distractorOptions
                .getOrNull(index)
                ?.let(result::add)
        }

        return result
    }

    private companion object {
        const val QUIZ_OPTIONS_COUNT = 6
    }
}