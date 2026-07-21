package com.example.phishingawareness.data.repository

import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.IndicatorDefinition

class AssetLibraryRepository(
    private val dataSource: LibraryAssetDataSource
) : LibraryRepository {

    private var cachedManifest: LibraryManifest? = null

    private var cachedScenarios:
            List<ScenarioDefinition>? = null

    private var cachedIndicators:
            List<IndicatorDefinition>? = null

    private var cachedDistractors:
            List<DistractorDefinition>? = null

    override fun getManifest(): LibraryManifest {
        return cachedManifest
            ?: dataSource.loadManifest().also {
                cachedManifest = it
            }
    }

    override fun getScenarios(): List<ScenarioDefinition> {
        return cachedScenarios
            ?: dataSource.loadScenarios().also {
                cachedScenarios = it
            }
    }

    override fun getEnabledScenarios(): List<ScenarioDefinition> {
        val manifest = getManifest()
        val activeScenarioIds =
            manifest.activeScenarios.toSet()

        return getScenarios().filter { scenario ->
            scenario.enabled &&
                    scenario.id in activeScenarioIds
        }
    }

    override fun getIndicators(): List<IndicatorDefinition> {
        return cachedIndicators
            ?: dataSource.loadIndicators().also {
                cachedIndicators = it
            }
    }

    override fun getDistractors(): List<DistractorDefinition> {
        return cachedDistractors
            ?: dataSource.loadDistractors().also {
                cachedDistractors = it
            }
    }

    override fun getIndicatorsForScenario(
        scenarioId: String
    ): List<IndicatorDefinition> {
        return getIndicators().filter { indicator ->
            indicator.enabled &&
                    indicator.observable &&
                    scenarioId in indicator.scenarios
        }
    }

    override fun getDistractorsForScenario(
        scenarioId: String
    ): List<DistractorDefinition> {
        return getDistractors().filter { distractor ->
            distractor.enabled &&
                    scenarioId in distractor.scenarios
        }
    }

    override fun getIndicatorById(
        indicatorId: String
    ): IndicatorDefinition? {
        return getIndicators().firstOrNull { indicator ->
            indicator.id == indicatorId
        }
    }

    override fun getIndicatorByPromptId(
        promptId: String
    ): IndicatorDefinition? {
        return getIndicators().firstOrNull { indicator ->
            indicator.promptId == promptId
        }
    }
}