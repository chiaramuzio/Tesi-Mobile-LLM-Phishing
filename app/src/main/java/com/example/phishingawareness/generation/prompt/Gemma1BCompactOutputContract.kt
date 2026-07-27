package com.example.phishingawareness.generation.prompt

/**
 * Contratto sperimentale compatto per Gemma 3 1B.
 *
 * Mantiene la struttura semantica richiesta dal parser applicativo,
 * evitando esempi di valore che il modello potrebbe copiare
 * letteralmente.
 *
 * Non sostituisce il contratto operativo usato dal modello 4B.
 */
object Gemma1BCompactOutputContract {

    val content: String =
        """
        FORMATO DI RISPOSTA

        Restituisci soltanto un oggetto JSON valido.
        Non usare Markdown o blocchi di codice.
        Il primo carattere deve essere {
        L'ultimo carattere deve essere }

        Usa tutte e sole queste chiavi di primo livello:
        scenario
        difficulty
        length
        sender_name
        sender_address
        recipient
        subject
        body
        pretext
        cta_type
        cta_text
        present_indicators
        credibility_elements
        educational_summary

        present_indicators deve essere un array non vuoto.
        Ogni elemento di present_indicators deve contenere:
        id
        evidence
        explanation

        credibility_elements deve essere un array non vuoto.

        Tutti i campi testuali devono essere compilati e non vuoti.
        Ogni evidence deve essere una citazione letterale presente nel body.
        Non aggiungere chiavi diverse da quelle indicate.
        Non copiare le istruzioni nella risposta.
        """.trimIndent()
}