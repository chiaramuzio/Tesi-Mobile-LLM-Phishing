package com.example.phishingawareness.data.repository

import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository

class FakeExerciseGenerationRepository :
    ExerciseGenerationRepository {

    override fun generateEmail(
        request: GenerationRequest
    ): GeneratedEmail {
        return when (request.scenarioId) {
            BANKING_SCENARIO_ID ->
                generateBankingEmail()

            ACCOUNT_IT_SCENARIO_ID ->
                generateAccountItEmail()

            else ->
                throw IllegalArgumentException(
                    "Scenario non supportato: ${request.scenarioId}"
                )
        }
    }

    private fun generateBankingEmail(): GeneratedEmail {
        return GeneratedEmail(
            senderName = "Servizio Sicurezza",
            senderAddress =
                "sicurezza@verifica-banca-online.com",
            subject =
                "Verifica immediata del conto richiesta",
            body = """
                Gentile cliente,

                abbiamo rilevato un accesso anomalo al suo conto.

                Per evitare la sospensione dei servizi, verifichi immediatamente la sua identità tramite il collegamento indicato.

                La mancata verifica entro oggi comporterà il blocco temporaneo del conto.

                Servizio Sicurezza
            """.trimIndent(),
            presentIndicatorIds = setOf(
                "SENDER_DOMAIN_MISMATCH",
                "URGENCY_PRESSURE",
                "ACCOUNT_SUSPENSION_THREAT"
            )
        )
    }

    private fun generateAccountItEmail(): GeneratedEmail {
        return GeneratedEmail(
            senderName = "Assistenza Informatica",
            senderAddress =
                "supporto@account-verifica-it.com",
            subject =
                "Aggiornamento urgente delle credenziali",
            body = """
                Gentile utente,

                il suo account aziendale richiede un aggiornamento immediato.

                Per evitare l'interruzione dell'accesso ai servizi, risponda entro oggi indicando nome utente, password e codice di verifica.

                In assenza di conferma, l'accesso potrebbe essere temporaneamente limitato.

                Assistenza Informatica
            """.trimIndent(),
            presentIndicatorIds = setOf(
                "SENDER_DOMAIN_MISMATCH",
                "URGENCY_PRESSURE",
                "SENSITIVE_DATA_REQUEST"
            )
        )
    }

    private companion object {
        const val BANKING_SCENARIO_ID = "BANKING"
        const val ACCOUNT_IT_SCENARIO_ID = "ACCOUNT_IT"
    }
}