package com.example.phishingawareness.domain.model

data class LibraryManifest(
    val libraryId: String,
    val version: String,
    val schemaVersion: Int,
    val language: String,
    val activeScenarios: List<String>,
    val resources: List<String>
)

data class ScenarioDefinition(
    val id: String,
    val displayName: String,
    val enabled: Boolean
)

data class IndicatorDefinition(
    val id: String,
    val promptId: String,
    val displayName: String,
    val description: String,
    val observable: Boolean,
    val enabled: Boolean,
    val scenarios: List<String>
)

data class DistractorDefinition(
    val id: String,
    val displayName: String,
    val description: String,
    val enabled: Boolean,
    val scenarios: List<String>
)