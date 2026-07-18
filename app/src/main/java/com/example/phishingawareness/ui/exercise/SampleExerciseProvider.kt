package com.example.phishingawareness.ui.exercise

import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.SimulatedEmail
import com.example.phishingawareness.domain.usecase.BuildQuizOptionsUseCase

class SampleExerciseProvider(
    private val buildQuizOptionsUseCase: BuildQuizOptionsUseCase
) {

    fun createExercise(
        scenarioId: String
    ): Exercise {
        return when (scenarioId) {
            ACCOUNT_IT_SCENARIO_ID ->
                createAccountItExercise()

            BANKING_SCENARIO_ID ->
                createBankingExercise()

            else ->
                throw IllegalArgumentException(
                    "Scenario non supportato: $scenarioId"
                )
        }
    }

    private fun createBankingExercise(): Exercise {
        val presentIndicatorIds =
            setOf(
                "SENDER_DOMAIN_MISMATCH",
                "URGENCY_PRESSURE",
                "ACCOUNT_SUSPENSION_THREAT"
            )

        return Exercise(
            email = SimulatedEmail(
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
                """.trimIndent()
            ),
            quizOptions = buildQuizOptionsUseCase(
                scenarioId = BANKING_SCENARIO_ID,
                presentIndicatorIds = presentIndicatorIds
            )
        )
    }

    private fun createAccountItExercise(): Exercise {
        val presentIndicatorIds =
            setOf(
                "SENDER_DOMAIN_MISMATCH",
                "URGENCY_PRESSURE",
                "SENSITIVE_DATA_REQUEST"
            )

        return Exercise(
            email = SimulatedEmail(
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
                """.trimIndent()
            ),
            quizOptions = buildQuizOptionsUseCase(
                scenarioId = ACCOUNT_IT_SCENARIO_ID,
                presentIndicatorIds = presentIndicatorIds
            )
        )
    }

    private companion object {
        const val BANKING_SCENARIO_ID = "BANKING"
        const val ACCOUNT_IT_SCENARIO_ID = "ACCOUNT_IT"
    }
}