package com.example.phishingawareness.domain.model

data class UserConfiguration(
    val scenario: Scenario,
    val difficulty: Difficulty,
    val length: ExerciseLength
)

enum class Scenario {
    BANKING,
    ACCOUNT_IT
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class ExerciseLength {
    SHORT,
    MEDIUM,
    LONG
}