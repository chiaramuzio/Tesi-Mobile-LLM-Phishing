package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptParameterResolutionRequest
import com.example.phishingawareness.domain.model.PromptParameterResolutionResult

interface PromptParameterResolver {

    fun resolve(
        request: PromptParameterResolutionRequest
    ): PromptParameterResolutionResult
}