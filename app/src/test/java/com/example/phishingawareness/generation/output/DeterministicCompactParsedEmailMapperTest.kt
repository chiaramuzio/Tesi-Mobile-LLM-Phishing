package com.example.phishingawareness.generation.output

import com.example.phishingawareness.domain.model.CompactParsedPhishingEmail
import com.example.phishingawareness.domain.model.CompactParsedPhishingIndicator
import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicCompactParsedEmailMapperTest {

    private val mapper =
        DeterministicCompactParsedEmailMapper()

    @Test
    fun map_validCompactParsedEmail_returnsGeneratedEmail() {
        val result =
            mapper.map(
                validCompactParsedEmail()
            )

        assertEquals(
            "Servizio Sicurezza Banca Esempio",
            result.senderName
        )

        assertEquals(
            "sicurezza@bancaesempio.invalid",
            result.senderAddress
        )

        assertEquals(
            "Verifica accesso sospetto",
            result.subject
        )

        assertEquals(
            "Corpo della simulazione",
            result.body
        )

        assertEquals(
            setOf(
                "URGENCY_PRESSURE",
                "SUSPICIOUS_LINK",
                "SENSITIVE_DATA_REQUEST"
            ),
            result.presentIndicatorIds
        )
    }

    @Test
    fun map_preservesOnlyInternalIndicatorIds() {
        val result =
            mapper.map(
                validCompactParsedEmail()
            )

        assertEquals(
            false,
            "IND_URGENCY" in result.presentIndicatorIds
        )

        assertEquals(
            true,
            "URGENCY_PRESSURE" in result.presentIndicatorIds
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptySenderName_throwsException() {
        mapper.map(
            validCompactParsedEmail().copy(
                senderName = ""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptySenderAddress_throwsException() {
        mapper.map(
            validCompactParsedEmail().copy(
                senderAddress = ""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptySubject_throwsException() {
        mapper.map(
            validCompactParsedEmail().copy(
                subject = ""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptyBody_throwsException() {
        mapper.map(
            validCompactParsedEmail().copy(
                body = ""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptyIndicators_throwsException() {
        mapper.map(
            validCompactParsedEmail().copy(
                presentIndicators = emptyList()
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_duplicateInternalIndicators_throwsException() {
        val duplicatedIndicator =
            CompactParsedPhishingIndicator(
                promptId = "IND_DUPLICATE",
                internalId = "URGENCY_PRESSURE",
                evidence = "Seconda evidence"
            )

        mapper.map(
            validCompactParsedEmail().copy(
                presentIndicators =
                    validCompactParsedEmail().presentIndicators +
                        duplicatedIndicator
            )
        )
    }

    private fun validCompactParsedEmail():
        CompactParsedPhishingEmail {
        return CompactParsedPhishingEmail(
            senderName =
                "Servizio Sicurezza Banca Esempio",
            senderAddress =
                "sicurezza@bancaesempio.invalid",
            subject =
                "Verifica accesso sospetto",
            body =
                "Corpo della simulazione",
            presentIndicators = listOf(
                CompactParsedPhishingIndicator(
                    promptId = "IND_URGENCY",
                    internalId = "URGENCY_PRESSURE",
                    evidence = "Evidence urgenza"
                ),
                CompactParsedPhishingIndicator(
                    promptId = "IND_SUSPICIOUS_LINK",
                    internalId = "SUSPICIOUS_LINK",
                    evidence = "Evidence link"
                ),
                CompactParsedPhishingIndicator(
                    promptId = "IND_CREDENTIAL_REQUEST",
                    internalId = "SENSITIVE_DATA_REQUEST",
                    evidence = "Evidence credenziali"
                )
            )
        )
    }
}
