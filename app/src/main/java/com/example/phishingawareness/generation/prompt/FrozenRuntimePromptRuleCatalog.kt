package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.RuntimePromptRuleSet
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.prompt.RuntimePromptRuleCatalog

object FrozenRuntimePromptRuleCatalog :
    RuntimePromptRuleCatalog {

    private val commonRules = listOf(
        "Mantieni coerenza tra scenario, mittente, pretesto, tono, firma e call to action.",
        "Utilizza soltanto persone, nomi, domini, collegamenti, numeri, account e codici chiaramente fittizi.",
        "Tutti i domini presenti nell'output devono terminare in .invalid.",
        "Non inserire numeri telefonici.",
        "Inserisci una sola call to action principale.",
        "Scrivi l'URL completo come testo semplice, senza sintassi Markdown o collegamenti mascherati.",
        "Inserisci esattamente i tre indicatori richiesti e non aggiungerne altri intenzionalmente.",
        "Ogni evidence deve essere una sottostringa letterale identica al testo sorgente.",
        "Ogni evidence deve provenire da una sola frase e dimostrare direttamente un solo indicatore.",
        "Il campo explanation deve spiegare in italiano perché l'evidence dimostra l'indicatore.",
        "Non generare i distrattori: saranno selezionati dall'applicazione.",
        "Restituisci direttamente e soltanto un oggetto JSON valido.",
        "Il primo carattere della risposta deve essere { e l'ultimo carattere deve essere }.",
        "Non usare blocchi Markdown, intestazioni o testo prima o dopo il JSON.",
        "Tutti i valori testuali dell'output devono essere scritti in italiano."
    )

    private val banking =
        RuntimePromptRuleSet(
            scenario = Scenario.BANKING,
            commonRules = commonRules,
            scenarioRules = listOf(
                "Usa esattamente il dominio bancaesempio.invalid nel sender_address, nel recipient e in tutti gli URL.",
                "Usa \"Banca Esempio\" nei campi testuali e \"bancaesempio.invalid\" soltanto negli indirizzi email e negli URL.",
                "Se citi un conto, usa soltanto la forma fittizia \"conto terminante in ****1234\".",
                "La call to action principale deve richiedere il login per verificare un accesso sospetto.",
                "cta_text deve essere esattamente \"Accedi al conto\".",
                "Il body deve contenere esattamente l'URL https://bancaesempio.invalid/login.",
                "L'URL deve comparire su una riga separata prima della firma.",
                "Nel body inserisci esattamente la frase: Accedi immediatamente per evitare la sospensione temporanea del conto.",
                "Per IND_URGENCY usa come evidence esattamente: Accedi immediatamente per evitare la sospensione temporanea del conto.",
                "Per IND_SUSPICIOUS_LINK usa come evidence esattamente: https://bancaesempio.invalid/login",
                "Nel body inserisci esattamente una volta la frase: Effettua il login con il tuo nome utente e la tua password per verificare l'accesso.",
                "Per IND_CREDENTIAL_REQUEST usa come evidence esattamente: Effettua il login con il tuo nome utente e la tua password per verificare l'accesso.",
                "sender_name deve essere esattamente \"Servizio Sicurezza Banca Esempio\".",
                "sender_address deve essere esattamente \"sicurezza@bancaesempio.invalid\".",
                "recipient deve essere esattamente \"utente@bancaesempio.invalid\".",
                "Il body deve terminare con la firma: Cordiali saluti, seguita da Servizio Sicurezza Banca Esempio.",
                "Il body deve descrivere un accesso non autorizzato e almeno un dettaglio tecnico plausibile.",
                "Il body deve contenere esattamente tre paragrafi prima della firma.",
                "Il body deve contenere tra 100 e 130 parole, esclusa la firma."
            ),
            internalChecks = listOf(
                "Tutti i domini siano bancaesempio.invalid.",
                "sender_name, sender_address, recipient e cta_text coincidano con i valori obbligatori.",
                "Il body contenga tutte e tre le evidence esatte.",
                "L'URL compaia letteralmente nel body su una riga separata.",
                "credibility_elements contenga esattamente i quattro valori richiesti nello stesso ordine.",
                "Il body termini con la firma obbligatoria."
            )
        )

    private val accountIt =
        RuntimePromptRuleSet(
            scenario = Scenario.ACCOUNT_IT,
            commonRules = commonRules,
            scenarioRules = listOf(
                "La call to action principale deve richiedere il login per aggiornare la password.",
                "Nel body inserisci esattamente la frase: Aggiorna la password immediatamente per evitare il blocco dell'account.",
                "Per IND_URGENCY usa come evidence esattamente: Aggiorna la password immediatamente per evitare il blocco dell'account.",
                "Per IND_SUSPICIOUS_LINK usa come evidence esclusivamente l'URL completo.",
                "Nel body inserisci esattamente la frase: Accedi con il tuo nome utente e la tua password per completare l'aggiornamento.",
                "Per IND_CREDENTIAL_REQUEST usa come evidence esattamente: Accedi con il tuo nome utente e la tua password per completare l'aggiornamento.",
                "sender_name deve contenere soltanto il nome del servizio o dell'organizzazione fittizia.",
                "sender_address deve usare il formato supporto@aziendaesempio.invalid.",
                "recipient deve usare il formato utente@aziendaesempio.invalid.",
                "Il riferimento a policy aziendali deve essere generico e fittizio.",
                "Il linguaggio tecnico deve restare comprensibile a un utente non specialista.",
                "Il collegamento deve essere coerente con Azienda Esempio e terminare in .invalid.",
                "Il body deve terminare con una firma esplicita del servizio IT."
            ),
            internalChecks = listOf(
                "Tutti i domini terminino in .invalid.",
                "Le evidence coincidano esattamente con il testo sorgente.",
                "sender_name non contenga nomi di persone.",
                "sender_address e recipient contengano soltanto indirizzi email.",
                "Tutti gli elementi di credibility_elements siano presenti nel messaggio.",
                "Il body termini con una firma esplicita del servizio IT."
            )
        )

    override fun get(
        scenario: Scenario
    ): RuntimePromptRuleSet {
        return when (scenario) {
            Scenario.BANKING -> banking
            Scenario.ACCOUNT_IT -> accountIt
        }
    }
}