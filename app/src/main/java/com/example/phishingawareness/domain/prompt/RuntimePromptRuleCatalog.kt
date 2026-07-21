package com.example.phishingawareness.domain.prompt

import com.example.phishingawareness.domain.model.RuntimePromptRuleSet
import com.example.phishingawareness.domain.model.Scenario

interface RuntimePromptRuleCatalog {

    fun get(
        scenario: Scenario
    ): RuntimePromptRuleSet?
}