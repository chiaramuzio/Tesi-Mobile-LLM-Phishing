package com.example.phishingawareness.data.repository

import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition

interface LibraryRepository {

    fun getManifest(): LibraryManifest

    fun getScenarios(): List<ScenarioDefinition>

    fun getEnabledScenarios(): List<ScenarioDefinition>
}