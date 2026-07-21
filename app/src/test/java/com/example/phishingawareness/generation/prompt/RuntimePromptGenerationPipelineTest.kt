package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.RuntimePromptGenerationFailureStage
import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.model.UserConfiguration
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePromptGenerationPipelineTest {

    private val repository =
        TestLibraryRepository(
            indicators = listOf(
                indicator(
                    id = "URGENCY_PRESSURE",
                    promptId = "IND_URGENCY"
                ),
                indicator(
                    id = "SUSPICIOUS_LINK",
                    promptId = "IND_SUSPICIOUS_LINK"
                ),
                indicator(
                    id = "SENSITIVE_DATA_REQUEST",
                    promptId = "IND_CREDENTIAL_REQUEST"
                )
            )
        )

    private val orchestrator =
        DeterministicRuntimePromptGenerationOrchestrator(
            parameterResolver =
                DeterministicPromptParameterResolver(
                    profileCatalog =
                        FrozenRuntimePromptProfileCatalog,
                    libraryRepository = repository
                ),
            sectionResolver =
                DeterministicRuntimePromptSectionResolver(),
            promptBuilder =
                DeterministicPromptBuilder()
        )

    @Test
    fun generate_bankingConfiguration_returnsPromptArtifact() {
        val result =
            orchestrator.generate(
                request(
                    scenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is RuntimePromptGenerationResult.Success
        )

        val artifact =
            (result as RuntimePromptGenerationResult.Success)
                .artifact

        assertTrue(
            artifact.text.contains(
                "Scenario: Bancario e pagamenti"
            )
        )

        assertTrue(
            artifact.text.contains(
                "Pretesto: Accesso sospetto"
            )
        )

        assertTrue(
            artifact.text.contains(
                "- IND_URGENCY"
            )
        )

        assertTrue(
            artifact.text.contains(
                "- IND_SUSPICIOUS_LINK"
            )
        )

        assertTrue(
            artifact.text.contains(
                "- IND_CREDENTIAL_REQUEST"
            )
        )

        assertTrue(
            artifact.text.endsWith("}")
        )

        assertEquals(
            "configuration-1",
            artifact.metadata.resolvedConfigurationId
        )

        assertEquals(
            64,
            artifact.metadata.promptSha256.length
        )
    }

    @Test
    fun generate_accountItConfiguration_returnsScenarioProfile() {
        val result =
            orchestrator.generate(
                request(
                    scenario = Scenario.ACCOUNT_IT
                )
            ) as RuntimePromptGenerationResult.Success

        val text = result.artifact.text

        assertTrue(
            text.contains(
                "Scenario: Account aziendale, webmail e servizi IT"
            )
        )

        assertTrue(
            text.contains(
                "Pretesto: Password in scadenza"
            )
        )

        assertTrue(
            text.contains(
                "Marchio nominale: Azienda Esempio"
            )
        )

        assertTrue(
            text.contains(
                "- firma del servizio IT"
            )
        )
    }

    @Test
    fun generate_unsupportedProfile_returnsParameterStageFailure() {
        val result =
            orchestrator.generate(
                request(
                    scenario = Scenario.BANKING,
                    difficulty = Difficulty.HARD
                )
            )

        assertTrue(
            result is RuntimePromptGenerationResult.Failure
        )

        val failure =
            result as RuntimePromptGenerationResult.Failure

        assertEquals(
            RuntimePromptGenerationFailureStage
                .PARAMETER_RESOLUTION,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "PROFILE_NOT_FOUND"
            )
        )
    }

    @Test
    fun generate_missingIndicator_returnsParameterStageFailure() {
        val failingOrchestrator =
            DeterministicRuntimePromptGenerationOrchestrator(
                parameterResolver =
                    DeterministicPromptParameterResolver(
                        profileCatalog =
                            FrozenRuntimePromptProfileCatalog,
                        libraryRepository =
                            TestLibraryRepository(
                                indicators = emptyList()
                            )
                    ),
                sectionResolver =
                    DeterministicRuntimePromptSectionResolver(),
                promptBuilder =
                    DeterministicPromptBuilder()
            )

        val result =
            failingOrchestrator.generate(
                request(
                    scenario = Scenario.BANKING
                )
            )

        val failure =
            result as RuntimePromptGenerationResult.Failure

        assertEquals(
            RuntimePromptGenerationFailureStage
                .PARAMETER_RESOLUTION,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "INDICATOR_NOT_FOUND"
            )
        )
    }

    @Test
    fun generate_sameRequestTwice_returnsSameTextAndHash() {
        val request =
            request(
                scenario = Scenario.BANKING
            )

        val first =
            orchestrator.generate(request)
                    as RuntimePromptGenerationResult.Success

        val second =
            orchestrator.generate(request)
                    as RuntimePromptGenerationResult.Success

        assertEquals(
            first.artifact.text,
            second.artifact.text
        )

        assertEquals(
            first.artifact.metadata.promptSha256,
            second.artifact.metadata.promptSha256
        )
    }

    private fun request(
        scenario: Scenario,
        difficulty: Difficulty = Difficulty.MEDIUM,
        length: ExerciseLength = ExerciseLength.MEDIUM
    ): RuntimePromptGenerationRequest {
        return RuntimePromptGenerationRequest(
            configurationId = "configuration-1",
            userConfiguration =
                UserConfiguration(
                    scenario = scenario,
                    difficulty = difficulty,
                    length = length
                ),
            language = "it",
            buildContext =
                PromptBuildContext(
                    builderVersion = "1",
                    templateId = "RUNTIME_MODULAR",
                    templateVersion = "1",
                    libraryId =
                        "phishing-awareness-library",
                    libraryVersion = "0.3.0",
                    librarySchemaVersion = 2
                )
        )
    }

    private fun indicator(
        id: String,
        promptId: String
    ): IndicatorDefinition {
        return IndicatorDefinition(
            id = id,
            promptId = promptId,
            displayName = id,
            description = id,
            observable = true,
            enabled = true,
            scenarios = listOf(
                Scenario.BANKING.name,
                Scenario.ACCOUNT_IT.name
            )
        )
    }

    private class TestLibraryRepository(
        private val indicators: List<IndicatorDefinition>
    ) : LibraryRepository {

        override fun getManifest(): LibraryManifest {
            error("Non usato nel test")
        }

        override fun getScenarios():
                List<ScenarioDefinition> {
            return emptyList()
        }

        override fun getEnabledScenarios():
                List<ScenarioDefinition> {
            return emptyList()
        }

        override fun getIndicators():
                List<IndicatorDefinition> {
            return indicators
        }

        override fun getDistractors():
                List<DistractorDefinition> {
            return emptyList()
        }

        override fun getIndicatorsForScenario(
            scenarioId: String
        ): List<IndicatorDefinition> {
            return indicators.filter { indicator ->
                scenarioId in indicator.scenarios
            }
        }

        override fun getDistractorsForScenario(
            scenarioId: String
        ): List<DistractorDefinition> {
            return emptyList()
        }

        override fun getIndicatorById(
            indicatorId: String
        ): IndicatorDefinition? {
            return indicators.firstOrNull { indicator ->
                indicator.id == indicatorId
            }
        }

        override fun getIndicatorByPromptId(
            promptId: String
        ): IndicatorDefinition? {
            return indicators.firstOrNull { indicator ->
                indicator.promptId == promptId
            }
        }
    }
}