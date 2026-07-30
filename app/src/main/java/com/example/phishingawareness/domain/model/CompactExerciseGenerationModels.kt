package com.example.phishingawareness.domain.model

sealed class CompactExerciseGenerationResult {

    data class Success(
        val exercise: Exercise,
        val promptMetadata: PromptMetadata,
        val executionMetadata:
        LocalModelExecutionMetadata
    ) : CompactExerciseGenerationResult()

    sealed class Failure :
        CompactExerciseGenerationResult() {

        data class LocalEmailGeneration(
            val stage:
            LocalEmailGenerationFailureStage,
            val details: String
        ) : Failure()

        data class QuizBuilding(
            val details: String
        ) : Failure()
    }
}