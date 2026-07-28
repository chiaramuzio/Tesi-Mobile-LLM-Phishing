package com.example.phishingawareness.domain.modeloutput

import com.example.phishingawareness.domain.model.CompactModelOutputParseRequest
import com.example.phishingawareness.domain.model.CompactModelOutputParseResult

interface CompactModelOutputParser {

    fun parse(
        request: CompactModelOutputParseRequest
    ): CompactModelOutputParseResult
}