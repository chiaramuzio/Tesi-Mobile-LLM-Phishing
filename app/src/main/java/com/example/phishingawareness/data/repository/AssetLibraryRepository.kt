package com.example.phishingawareness.data.repository

import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition

class AssetLibraryRepository(
    private val dataSource: LibraryAssetDataSource
) : LibraryRepository {

    private var cachedManifest: LibraryManifest? = null

    private var cachedScenarios:
            List<ScenarioDefinition>? = null

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
}