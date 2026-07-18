package com.example.phishingawareness.domain.model

data class LibraryManifest(
    val libraryId: String,
    val version: String,
    val schemaVersion: Int,
    val language: String,
    val activeScenarios: List<String>
)

data class ScenarioDefinition(
    val id: String,
    val displayName: String,
    val enabled: Boolean
)