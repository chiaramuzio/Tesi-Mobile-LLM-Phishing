package com.example.phishingawareness.generation.output

import com.example.phishingawareness.domain.model.CompactParsedPhishingEmail
import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.modeloutput.CompactParsedEmailMapper

class DeterministicCompactParsedEmailMapper :
    CompactParsedEmailMapper {

    override fun map(
        email: CompactParsedPhishingEmail
    ): GeneratedEmail {
        require(email.senderName.isNotBlank()) {
            "senderName non può essere vuoto"
        }

        require(email.senderAddress.isNotBlank()) {
            "senderAddress non può essere vuoto"
        }

        require(email.subject.isNotBlank()) {
            "subject non può essere vuoto"
        }

        require(email.body.isNotBlank()) {
            "body non può essere vuoto"
        }

        require(email.presentIndicators.isNotEmpty()) {
            "presentIndicators non può essere vuoto"
        }

        val internalIndicatorIds =
            email.presentIndicators
                .map { indicator ->
                    indicator.internalId
                }

        require(
            internalIndicatorIds.distinct().size ==
                internalIndicatorIds.size
        ) {
            "Gli indicatori interni devono essere univoci"
        }

        return GeneratedEmail(
            senderName = email.senderName,
            senderAddress = email.senderAddress,
            subject = email.subject,
            body = email.body,
            presentIndicatorIds =
                internalIndicatorIds.toSet()
        )
    }
}
