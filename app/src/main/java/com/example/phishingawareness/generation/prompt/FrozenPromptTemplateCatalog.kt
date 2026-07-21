package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateReference
import com.example.phishingawareness.domain.model.Scenario

/**
 * Catalogo dei template zero-shot congelati utilizzati dal progetto.
 *
 * Ogni riferimento collega un identificativo stabile al relativo file
 * presente negli asset Android. Il catalogo non legge né modifica i file.
 */
object FrozenPromptTemplateCatalog {

    val bankingZeroShotV12 = PromptTemplateReference(
        id = PromptTemplateId.BANKING_ZERO_SHOT_V12,
        version = "12",
        scenario = Scenario.BANKING,
        assetPath = "prompts/zero_shot/BANKING_01_ZERO_SHOT_v12.txt"
    )

    val accountItZeroShotV3 = PromptTemplateReference(
        id = PromptTemplateId.ACCOUNT_IT_ZERO_SHOT_V3,
        version = "3",
        scenario = Scenario.ACCOUNT_IT,
        assetPath = "prompts/zero_shot/ACCOUNT_IT_01_ZERO_SHOT_v3.txt"
    )

    val all: List<PromptTemplateReference> = listOf(
        bankingZeroShotV12,
        accountItZeroShotV3
    )

    fun get(
        templateId: PromptTemplateId
    ): PromptTemplateReference? {
        return all.firstOrNull { reference ->
            reference.id == templateId
        }
    }
}