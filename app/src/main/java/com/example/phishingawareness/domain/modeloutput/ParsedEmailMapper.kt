package com.example.phishingawareness.domain.modeloutput

import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.ParsedPhishingEmail

interface ParsedEmailMapper {

    fun map(
        email: ParsedPhishingEmail
    ): GeneratedEmail
}