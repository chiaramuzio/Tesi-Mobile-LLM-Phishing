package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.RuntimePromptProfile
import com.example.phishingawareness.domain.model.Scenario

interface RuntimePromptProfileCatalog {

    fun get(
        scenario: Scenario,
        difficulty: Difficulty,
        length: ExerciseLength
    ): RuntimePromptProfile?
}