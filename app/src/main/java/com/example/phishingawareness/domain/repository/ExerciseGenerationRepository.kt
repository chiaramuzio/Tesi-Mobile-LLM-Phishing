package com.example.phishingawareness.domain.repository

import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest

interface ExerciseGenerationRepository {

    fun generateEmail(
        request: GenerationRequest
    ): GeneratedEmail
}