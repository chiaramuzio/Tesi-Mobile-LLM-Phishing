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
        Rispondi solo con JSON valido, senza Markdown.
        Chiavi esatte: scenario,difficulty,length,sender_name,sender_address,recipient,subject,body,pretext,cta_type,cta_text,present_indicators,credibility_elements,educational_summary.
        present_indicators deve contenere esattamente 3 oggetti con chiavi id,evidence,explanation.
        credibility_elements deve essere un array non vuoto.
        Tutti i campi devono essere compilati.
        Ogni evidence deve comparire letteralmente nel body.
        Nessuna chiave aggiuntiva.
        """.trimIndent()
}