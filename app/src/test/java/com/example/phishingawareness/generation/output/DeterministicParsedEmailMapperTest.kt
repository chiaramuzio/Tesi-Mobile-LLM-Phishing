package com.example.phishingawareness.generation.output

import com.example.phishingawareness.domain.model.ParsedPhishingEmail
import com.example.phishingawareness.domain.model.ParsedPhishingIndicator
import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicParsedEmailMapperTest {

    private val mapper =
        DeterministicParsedEmailMapper()

    @Test
    fun map_validParsedEmail_returnsGeneratedEmail() {
        val result =
            mapper.map(
                validParsedEmail()
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
                validParsedEmail()
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
            validParsedEmail().copy(
                senderName = ""
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_emptyIndicators_throwsException() {
        mapper.map(
            validParsedEmail().copy(
                presentIndicators = emptyList()
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_duplicateInternalIndicators_throwsException() {
        val duplicatedIndicator =
            ParsedPhishingIndicator(
                promptId = "IND_DUPLICATE",
                internalId = "URGENCY_PRESSURE",
                evidence = "Seconda evidence",
                explanation = "Seconda spiegazione"
            )

        mapper.map(
            validParsedEmail().copy(
                presentIndicators =
                    validParsedEmail().presentIndicators +
                            duplicatedIndicator
            )
        )
    }

    private fun validParsedEmail(): ParsedPhishingEmail {
        return ParsedPhishingEmail(
            scenario = "Bancario e pagamenti",
            difficulty = "Media",
            length = "Media",
            senderName =
                "Servizio Sicurezza Banca Esempio",
            senderAddress =
                "sicurezza@bancaesempio.invalid",
            recipient =
                "utente@bancaesempio.invalid",
            subject =
                "Verifica accesso sospetto",
            body =
                "Corpo della simulazione",
            pretext =
                "Accesso sospetto",
            ctaType =
                "LOGIN",
            ctaText =
                "Accedi al conto",
            presentIndicators = listOf(
                ParsedPhishingIndicator(
                    promptId = "IND_URGENCY",
                    internalId = "URGENCY_PRESSURE",
                    evidence = "Evidence urgenza",
                    explanation = "Spiegazione urgenza"
                ),
                ParsedPhishingIndicator(
                    promptId = "IND_SUSPICIOUS_LINK",
                    internalId = "SUSPICIOUS_LINK",
                    evidence = "Evidence link",
                    explanation = "Spiegazione link"
                ),
                ParsedPhishingIndicator(
                    promptId = "IND_CREDENTIAL_REQUEST",
                    internalId = "SENSITIVE_DATA_REQUEST",
                    evidence = "Evidence credenziali",
                    explanation = "Spiegazione credenziali"
                )
            ),
            credibilityElements = listOf(
                "tono professionale",
                "pretesto plausibile",
                "firma del servizio",
                "riferimento alla sicurezza del conto"
            ),
            educationalSummary =
                "Controllare mittente, collegamenti e richieste."
        )
    }
}