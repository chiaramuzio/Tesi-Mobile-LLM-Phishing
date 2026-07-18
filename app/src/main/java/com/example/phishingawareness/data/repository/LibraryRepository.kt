package com.example.phishingawareness.data.repository

import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.IndicatorDefinition

interface LibraryRepository {

    fun getManifest(): LibraryManifest

    fun getScenarios(): List<ScenarioDefinition>

    fun getEnabledScenarios(): List<ScenarioDefinition>

    fun getIndicators(): List<IndicatorDefinition>

    fun getDistractors(): List<DistractorDefinition>

    fun getIndicatorsForScenario(
        scenarioId: String
    ): List<IndicatorDefinition>

    fun getDistractorsForScenario(
        scenarioId: String
    ): List<DistractorDefinition>
}