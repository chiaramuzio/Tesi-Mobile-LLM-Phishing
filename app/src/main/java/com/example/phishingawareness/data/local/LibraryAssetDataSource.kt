package com.example.phishingawareness.data.local

import android.content.Context
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.ScenarioDefinition
import org.json.JSONObject
import java.io.IOException
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.IndicatorDefinition

class LibraryAssetDataSource(
    private val context: Context
) {

    fun loadManifest(): LibraryManifest {
        val json = readAssetFile(MANIFEST_PATH)
        val root = JSONObject(json)

        val activeScenarios =
            readStringList(
                root,
                "activeScenarios"
            )

        return LibraryManifest(
            libraryId = root.getString("libraryId"),
            version = root.getString("version"),
            schemaVersion = root.getInt("schemaVersion"),
            language = root.getString("language"),
            activeScenarios = activeScenarios,
            resources = readStringList(
                root,
                "resources"
            )
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

    fun loadIndicators(): List<IndicatorDefinition> {
        val json = readAssetFile(INDICATORS_PATH)
        val root = JSONObject(json)
        val indicatorsJson = root.getJSONArray("indicators")

        return buildList {
            for (index in 0 until indicatorsJson.length()) {
                val indicatorJson =
                    indicatorsJson.getJSONObject(index)

                add(
                    IndicatorDefinition(
                        id = indicatorJson.getString("id"),
                        promptId =
                            indicatorJson.getString("promptId"),
                        displayName =
                            indicatorJson.getString("displayName"),
                        description =
                            indicatorJson.getString("description"),
                        observable =
                            indicatorJson.getBoolean("observable"),
                        enabled =
                            indicatorJson.getBoolean("enabled"),
                        scenarios =
                            readStringList(
                                indicatorJson,
                                "scenarios"
                            )
                    )
                )
            }
        }
    }

    fun loadDistractors(): List<DistractorDefinition> {
        val json = readAssetFile(DISTRACTORS_PATH)
        val root = JSONObject(json)
        val distractorsJson = root.getJSONArray("distractors")

        return buildList {
            for (index in 0 until distractorsJson.length()) {
                val distractorJson =
                    distractorsJson.getJSONObject(index)

                add(
                    DistractorDefinition(
                        id = distractorJson.getString("id"),
                        displayName =
                            distractorJson.getString("displayName"),
                        description =
                            distractorJson.getString("description"),
                        enabled =
                            distractorJson.getBoolean("enabled"),
                        scenarios =
                            readStringList(
                                distractorJson,
                                "scenarios"
                            )
                    )
                )
            }
        }
    }

    private fun readStringList(
        jsonObject: JSONObject,
        fieldName: String
    ): List<String> {
        val jsonArray =
            jsonObject.getJSONArray(fieldName)

        return buildList {
            for (index in 0 until jsonArray.length()) {
                add(jsonArray.getString(index))
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

        const val INDICATORS_PATH =
            "library/indicators.json"

        const val DISTRACTORS_PATH =
            "library/distractors.json"
    }
}

class LibraryAssetException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)