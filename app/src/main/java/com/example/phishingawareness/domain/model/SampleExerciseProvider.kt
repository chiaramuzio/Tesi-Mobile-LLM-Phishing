package com.example.phishingawareness.domain.model

object SampleExerciseProvider {

    fun create(): Exercise {
        return Exercise(
            email = SimulatedEmail(
                senderName = "Servizio Sicurezza Conto",
                senderAddress = "assistenza@verifica-conto.example",
                subject = "Accesso insolito rilevato",
                body = """
                    Gentile cliente,

                    abbiamo rilevato un accesso insolito al suo conto.
                    Per evitare la sospensione temporanea, confermi subito
                    la sua identità tramite il collegamento indicato.

                    La verifica deve essere completata entro 30 minuti.

                    Servizio Sicurezza
                """.trimIndent()
            ),
            quizOptions = listOf(
                QuizOption(
                    id = "sender_domain",
                    text = "Il dominio del mittente è sospetto",
                    isCorrect = true
                ),
                QuizOption(
                    id = "urgency",
                    text = "Il messaggio crea urgenza",
                    isCorrect = true
                ),
                QuizOption(
                    id = "suspension_threat",
                    text = "Minaccia la sospensione del conto",
                    isCorrect = true
                ),
                QuizOption(
                    id = "personalized_name",
                    text = "Il messaggio usa correttamente nome e cognome",
                    isCorrect = false
                ),
                QuizOption(
                    id = "official_attachment",
                    text = "È presente un allegato ufficiale firmato",
                    isCorrect = false
                ),
                QuizOption(
                    id = "known_contact",
                    text = "Il mittente è un contatto già conosciuto",
                    isCorrect = false
                )
            )
        )
    }
}