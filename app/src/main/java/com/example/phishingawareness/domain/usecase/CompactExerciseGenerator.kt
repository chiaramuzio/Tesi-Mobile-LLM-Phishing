package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.GenerationRequest

interface CompactExerciseGenerator {

    fun generate(
        request: GenerationRequest
    ): CompactExerciseGenerationResult
}