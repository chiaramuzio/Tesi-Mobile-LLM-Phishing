package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult

interface RuntimePromptSectionResolver {

    fun resolve(
        parameters: ResolvedPromptParameters
    ): RuntimePromptSectionResolutionResult
}
