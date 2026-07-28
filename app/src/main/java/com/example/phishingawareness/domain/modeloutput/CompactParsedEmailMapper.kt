package com.example.phishingawareness.domain.modeloutput

import com.example.phishingawareness.domain.model.CompactParsedPhishingEmail
import com.example.phishingawareness.domain.model.GeneratedEmail

interface CompactParsedEmailMapper {

    fun map(
        email: CompactParsedPhishingEmail
    ): GeneratedEmail
}