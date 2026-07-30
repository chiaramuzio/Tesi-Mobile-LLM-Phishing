package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult

interface CompactLocalEmailGenerator {

    operator fun invoke(
        request: GenerationRequest,
        options: LocalEmailGenerationOptions =
            LocalEmailGenerationOptions()
    ): LocalEmailGenerationResult
}