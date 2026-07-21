package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult

interface RuntimePromptGenerationOrchestrator {

    fun generate(
        request: RuntimePromptGenerationRequest
    ): RuntimePromptGenerationResult
}