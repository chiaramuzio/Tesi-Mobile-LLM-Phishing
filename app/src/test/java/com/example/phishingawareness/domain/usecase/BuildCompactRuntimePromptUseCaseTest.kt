package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationFailureStage
import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildCompactRuntimePromptUseCaseTest {

    private val repository =
        TestLibraryRepository()

    @Test
    fun invoke_validRequest_usesCompactTemplateMetadata() {
        val orchestrator =
            CapturingOrchestrator()

        val useCase =
            BuildCompactRuntimePromptUseCase(
                orchestrator = orchestrator,
                libraryRepository = repository
            )

        useCase(
            request = GenerationRequest(
                scenarioId = "ACCOUNT_IT",
                difficulty = "MEDIUM",
                length = "MEDIUM"
            )
        )

        val receivedRequest =
            orchestrator.receivedRequest

        assertNotNull(receivedRequest)

        assertEquals(
            "ACCOUNT_IT_MEDIUM_MEDIUM",
            receivedRequest?.configurationId
        )

        assertEquals(
            "RUNTIME_MODULAR_COMPACT",
            receivedRequest
                ?.buildContext
                ?.templateId
        )

        assertEquals(
            "1",
            receivedRequest
                ?.buildContext
                ?.templateVersion
        )

        assertEquals(
            "1",
            receivedRequest
                ?.buildContext
                ?.builderVersion
        )

        assertEquals(
            "phishing-awareness-library",
            receivedRequest
                ?.buildContext
                ?.libraryId
        )

        assertEquals(
            "0.3.0",
            receivedRequest
                ?.buildContext
                ?.libraryVersion
        )

        assertEquals(
            2,
            receivedRequest
                ?.buildContext
                ?.librarySchemaVersion
        )

        assertEquals(
            "it",
            receivedRequest?.language
        )
    }

    @Test
    fun invoke_lowercaseValues_mapsConfiguration() {
        val orchestrator =
            CapturingOrchestrator()

        val useCase =
            BuildCompactRuntimePromptUseCase(
                orchestrator = orchestrator,
                libraryRepository = repository
            )

        useCase(
            request = GenerationRequest(
                scenarioId = "account_it",
                difficulty = "medium",
                length = "medium"
            )
        )

        assertEquals(
            "ACCOUNT_IT_MEDIUM_MEDIUM",
            orchestrator
                .receivedRequest
                ?.configurationId
        )
    }

    @Test
    fun invoke_unknownScenario_returnsMappingFailure() {
        val useCase =
            BuildCompactRuntimePromptUseCase(
                orchestrator =
                    CapturingOrchestrator(),
                libraryRepository = repository
            )

        val result =
            useCase(
                request = GenerationRequest(
                    scenarioId = "UNKNOWN",
                    difficulty = "MEDIUM",
                    length = "MEDIUM"
                )
            )

        assertTrue(
            result is RuntimePromptGenerationResult.Failure
        )

        val failure =
            result as RuntimePromptGenerationResult.Failure

        assertEquals(
            RuntimePromptGenerationFailureStage
                .REQUEST_MAPPING,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "scenarioId=UNKNOWN"
            )
        )
    }

    @Test
    fun invoke_unknownDifficulty_returnsMappingFailure() {
        val useCase =
            BuildCompactRuntimePromptUseCase(
                orchestrator =
                    CapturingOrchestrator(),
                libraryRepository = repository
            )

        val result =
            useCase(
                request = GenerationRequest(
                    scenarioId = "ACCOUNT_IT",
                    difficulty = "UNKNOWN",
                    length = "MEDIUM"
                )
            )

        val failure =
            result as RuntimePromptGenerationResult.Failure

        assertEquals(
            RuntimePromptGenerationFailureStage
                .REQUEST_MAPPING,
            failure.stage
        )
    }

    @Test
    fun invoke_returnsOrchestratorResult() {
        val expectedResult =
            RuntimePromptGenerationResult.Failure(
                stage =
                    RuntimePromptGenerationFailureStage
                        .PROMPT_BUILDING,
                details = "errore controllato"
            )

        val useCase =
            BuildCompactRuntimePromptUseCase(
                orchestrator =
                    CapturingOrchestrator(
                        result = expectedResult
                    ),
                libraryRepository = repository
            )

        val result =
            useCase(
                request = GenerationRequest(
                    scenarioId = "ACCOUNT_IT",
                    difficulty = "MEDIUM",
                    length = "MEDIUM"
                )
            )

        assertEquals(
            expectedResult,
            result
        )
    }

    private class CapturingOrchestrator(
        private val result:
        RuntimePromptGenerationResult =
            RuntimePromptGenerationResult.Failure(
                stage =
                    RuntimePromptGenerationFailureStage
                        .PROMPT_BUILDING,
                details = "risultato di test"
            )
    ) : RuntimePromptGenerationOrchestrator {

        var receivedRequest:
                RuntimePromptGenerationRequest? = null
            private set

        override fun generate(
            request: RuntimePromptGenerationRequest
        ): RuntimePromptGenerationResult {
            receivedRequest = request
            return result
        }
    }

    private class TestLibraryRepository :
        LibraryRepository {

        override fun getManifest(): LibraryManifest {
            return LibraryManifest(
                libraryId =
                    "phishing-awareness-library",
                version = "0.3.0",
                schemaVersion = 2,
                language = "it",
                activeScenarios = listOf(
                    "BANKING",
                    "ACCOUNT_IT"
                ),
                resources = listOf(
                    "scenarios.json",
                    "indicators.json",
                    "distractors.json"
                )
            )
        }

        override fun getScenarios():
            List<ScenarioDefinition> =
            emptyList()

        override fun getEnabledScenarios():
            List<ScenarioDefinition> =
            emptyList()

        override fun getIndicators():
            List<IndicatorDefinition> =
            emptyList()

        override fun getDistractors():
            List<DistractorDefinition> =
            emptyList()

        override fun getIndicatorsForScenario(
            scenarioId: String
        ): List<IndicatorDefinition> =
            emptyList()

        override fun getDistractorsForScenario(
            scenarioId: String
        ): List<DistractorDefinition> =
            emptyList()

        override fun getIndicatorById(
            indicatorId: String
        ): IndicatorDefinition? =
            null

        override fun getIndicatorByPromptId(
            promptId: String
        ): IndicatorDefinition? =
            null
    }
}
