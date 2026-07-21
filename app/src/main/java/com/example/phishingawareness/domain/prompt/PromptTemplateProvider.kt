package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId

/**
 * Fornisce template operativi già definiti e versionati.
 *
 * Il provider non seleziona autonomamente il template e non modifica
 * il suo contenuto. Riceve un identificativo esplicito e restituisce
 * il template corrispondente, quando disponibile.
 */
interface PromptTemplateProvider {

    fun get(
        templateId: PromptTemplateId
    ): PromptTemplate?
}