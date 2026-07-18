package com.example.phishingawareness.data.local

import android.content.Context
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition
import org.json.JSONObject
import java.io.IOException

class LibraryAssetDataSource(
    private val context: Context
) {

    fun loadManifest(): LibraryManifest {
        val json = readAssetFile(MANIFEST_PATH)
        val root = JSONObject(json)

        val activeScenariosJson =
            root.getJSONArray("activeScenarios")

        val activeScenarios =
            buildList {
                for (index in 0 until activeScenariosJson.length()) {
                    add(activeScenariosJson.getString(index))
                }
            }

        return LibraryManifest(
            libraryId = root.getString("libraryId"),
            version = root.getString("version"),
            schemaVersion = root.getInt("schemaVersion"),
            language = root.getString("language"),
            activeScenarios = activeScenarios
        )
    }

    fun loadScenarios(): List<ScenarioDefinition> {
        val json = readAssetFile(SCENARIOS_PATH)
        val root = JSONObject(json)
        val scenariosJson = root.getJSONArray("scenarios")

        return buildList {
            for (index in 0 until scenariosJson.length()) {
                val scenarioJson =
                    scenariosJson.getJSONObject(index)

                add(
                    ScenarioDefinition(
                        id = scenarioJson.getString("id"),
                        displayName =
                            scenarioJson.getString("displayName"),
                        enabled = scenarioJson.getBoolean("enabled")
                    )
                )
            }
        }
    }

    private fun readAssetFile(path: String): String {
        try {
            return context.assets
                .open(path)
                .bufferedReader()
                .use { reader ->
                    reader.readText()
                }
        } catch (exception: IOException) {
            throw LibraryAssetException(
                message = "Impossibile leggere il file asset: $path",
                cause = exception
            )
        }
    }

    private companion object {
        const val MANIFEST_PATH =
            "library/manifest.json"

        const val SCENARIOS_PATH =
            "library/scenarios.json"
    }
}

class LibraryAssetException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)