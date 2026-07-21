package com.example.phishingawareness.domain.modelruntime

import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult

interface LocalModelExecutor {

    fun execute(
        request: LocalModelExecutionRequest
    ): LocalModelExecutionResult
}