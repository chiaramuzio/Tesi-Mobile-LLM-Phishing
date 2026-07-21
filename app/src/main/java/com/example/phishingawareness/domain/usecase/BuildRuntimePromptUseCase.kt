package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.RuntimePromptGenerationFailureStage
import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.model.UserConfiguration
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.LibraryRepository

class BuildRuntimePromptUseCase(
    private val orchestrator: RuntimePromptGenerationOrchestrator,
    private val libraryRepository: LibraryRepository
) {

    operator fun invoke(
        request: GenerationRequest
    ): RuntimePromptGenerationResult {
        val userConfiguration =
            mapUserConfiguration(request)
                ?: return RuntimePromptGenerationResult.Failure(
                    stage =
                        RuntimePromptGenerationFailureStage
                            .REQUEST_MAPPING,
                    details =
                        buildMappingFailureDetails(request)
                )

        val manifest = libraryRepository.getManifest()

        return orchestrator.generate(
            request = RuntimePromptGenerationRequest(
                configurationId =
                    buildConfigurationId(userConfiguration),
                userConfiguration = userConfiguration,
                language = manifest.language,
                buildContext = PromptBuildContext(
                    builderVersion = BUILDER_VERSION,
                    templateId = RUNTIME_TEMPLATE_ID,
                    templateVersion = RUNTIME_TEMPLATE_VERSION,
                    libraryId = manifest.libraryId,
                    libraryVersion = manifest.version,
                    librarySchemaVersion =
                        manifest.schemaVersion
                )
            )
        )
    }

    private fun mapUserConfiguration(
        request: GenerationRequest
    ): UserConfiguration? {
        val scenario =
            enumValueOrNull<Scenario>(
                request.scenarioId
            ) ?: return null

        val difficulty =
            enumValueOrNull<Difficulty>(
                request.difficulty
            ) ?: return null

        val length =
            enumValueOrNull<ExerciseLength>(
                request.length
            ) ?: return null

        return UserConfiguration(
            scenario = scenario,
            difficulty = difficulty,
            length = length
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(
        value: String
    ): T? {
        val normalizedValue =
            value.trim().uppercase()

        return enumValues<T>().firstOrNull { enumValue ->
            enumValue.name == normalizedValue
        }
    }

    private fun buildConfigurationId(
        configuration: UserConfiguration
    ): String {
        return listOf(
            configuration.scenario.name,
            configuration.difficulty.name,
            configuration.length.name
        ).joinToString(separator = "_")
    }

    private fun buildMappingFailureDetails(
        request: GenerationRequest
    ): String {
        return "Richiesta non valida: " +
                "scenarioId=${request.scenarioId}, " +
                "difficulty=${request.difficulty}, " +
                "length=${request.length}"
    }

    private companion object {
        const val BUILDER_VERSION = "1"
        const val RUNTIME_TEMPLATE_ID =
            "RUNTIME_MODULAR"
        const val RUNTIME_TEMPLATE_VERSION = "1"
    }
}