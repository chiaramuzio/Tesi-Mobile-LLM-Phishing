package com.example.phishingawareness.domain.modeloutput

import com.example.phishingawareness.domain.model.ModelOutputParseRequest
import com.example.phishingawareness.domain.model.ModelOutputParseResult

interface ModelOutputParser {

    fun parse(
        request: ModelOutputParseRequest
    ): ModelOutputParseResult
}