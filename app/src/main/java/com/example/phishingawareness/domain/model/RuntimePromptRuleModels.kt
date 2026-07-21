package com.example.phishingawareness.domain.model

data class RuntimePromptRuleSet(
    val scenario: Scenario,
    val commonRules: List<String>,
    val scenarioRules: List<String>,
    val internalChecks: List<String>
)