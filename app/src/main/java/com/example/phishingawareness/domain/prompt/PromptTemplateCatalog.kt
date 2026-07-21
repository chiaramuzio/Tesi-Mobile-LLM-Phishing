package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateReference

/**
 * Fornisce i riferimenti ai template disponibili.
 *
 * Il catalogo espone esclusivamente metadati e percorsi logici.
 * Non legge il contenuto dei prompt e non costruisce template completi.
 */
interface PromptTemplateCatalog {

    fun get(
        templateId: PromptTemplateId
    ): PromptTemplateReference?
}