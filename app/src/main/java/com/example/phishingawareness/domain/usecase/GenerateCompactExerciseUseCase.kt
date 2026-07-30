package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.SimulatedEmail

class GenerateCompactExerciseUseCase(
    private val compactLocalEmailGenerator:
    CompactLocalEmailGenerator,
    private val buildQuizOptionsUseCase:
    BuildQuizOptionsUseCase
) {

    operator fun invoke(
        request: GenerationRequest,
        options: LocalEmailGenerationOptions =
            compactExerciseOptions()
    ): CompactExerciseGenerationResult {
        val emailResult =
            compactLocalEmailGenerator(
                request = request,
                options = options
            )

        val emailSuccess =
            when (emailResult) {
                is LocalEmailGenerationResult.Success ->
                    emailResult

                is LocalEmailGenerationResult.Failure ->
                    return CompactExerciseGenerationResult
                        .Failure
                        .LocalEmailGeneration(
                            stage = emailResult.stage,
                            details = emailResult.details
                        )
            }

        val generatedEmail =
            emailSuccess.email

        val quizOptions =
            try {
                buildQuizOptionsUseCase(
                    scenarioId = request.scenarioId,
                    presentIndicatorIds =
                        generatedEmail.presentIndicatorIds
                )
            } catch (
                exception: IllegalArgumentException
            ) {
                return CompactExerciseGenerationResult
                    .Failure
                    .QuizBuilding(
                        details =
                            exception.message
                                ?: "Errore durante la costruzione del quiz"
                    )
            }

        val exercise =
            Exercise(
                email =
                    SimulatedEmail(
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

        return CompactExerciseGenerationResult.Success(
            exercise = exercise,
            promptMetadata =
                emailSuccess.promptMetadata,
            executionMetadata =
                emailSuccess.executionMetadata
        )
    }

    private companion object {

        fun compactExerciseOptions():
                LocalEmailGenerationOptions {
            return LocalEmailGenerationOptions(
                seed = 101,
                contextSize = 8192,
                maxGeneratedTokens = 600,
                temperature = 0.4f,
                topK = 40,
                topP = 0.90f,
                minP = 0.05f,
                repeatPenalty = 1.05f
            )
        }
    }
}