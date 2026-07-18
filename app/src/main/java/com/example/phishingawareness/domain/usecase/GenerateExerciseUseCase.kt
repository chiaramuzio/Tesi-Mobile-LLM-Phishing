package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.SimulatedEmail
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository

class GenerateExerciseUseCase(
    private val generationRepository:
    ExerciseGenerationRepository,
    private val buildQuizOptionsUseCase:
    BuildQuizOptionsUseCase
) {

    operator fun invoke(
        request: GenerationRequest
    ): Exercise {
        val generatedEmail =
            generationRepository.generateEmail(
                request = request
            )

        val quizOptions =
            buildQuizOptionsUseCase(
                scenarioId = request.scenarioId,
                presentIndicatorIds =
                    generatedEmail.presentIndicatorIds
            )

        return Exercise(
            email = SimulatedEmail(
                senderName =
                    generatedEmail.senderName,
                senderAddress =
                    generatedEmail.senderAddress,
                subject =
                    generatedEmail.subject,
                body =
                    generatedEmail.body
            ),
            quizOptions = quizOptions
        )
    }
}