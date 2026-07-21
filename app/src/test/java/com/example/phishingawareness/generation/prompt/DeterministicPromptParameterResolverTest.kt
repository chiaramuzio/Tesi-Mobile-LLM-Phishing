package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.PromptParameterResolutionIssueCode
import com.example.phishingawareness.domain.model.PromptParameterResolutionRequest
import com.example.phishingawareness.domain.model.PromptParameterResolutionResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.model.UserConfiguration
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicPromptParameterResolverTest {

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

    private val resolver =
        DeterministicPromptParameterResolver(
            profileCatalog =
                FrozenRuntimePromptProfileCatalog,
            libraryRepository = repository
        )

    @Test
    fun resolve_bankingProfile_returnsExpectedParameters() {
        val result =
            resolver.resolve(
                request(
                    scenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is PromptParameterResolutionResult.Success
        )

        val parameters =
            (result as PromptParameterResolutionResult.Success)
                .parameters

        assertEquals(
            "BANKING_MEDIUM_MEDIUM_V1",
            parameters.profileId
        )
        assertEquals(
            "Accesso sospetto",
            parameters.pretext
        )
        assertEquals(
            listOf(
                "IND_URGENCY",
                "IND_SUSPICIOUS_LINK",
                "IND_CREDENTIAL_REQUEST"
            ),
            parameters.requiredIndicatorPromptIds
        )
    }

    @Test
    fun resolve_accountItProfile_returnsExpectedParameters() {
        val result =
            resolver.resolve(
                request(
                    scenario = Scenario.ACCOUNT_IT
                )
            )

        val parameters =
            (result as PromptParameterResolutionResult.Success)
                .parameters

        assertEquals(
            "ACCOUNT_IT_MEDIUM_MEDIUM_V1",
            parameters.profileId
        )
        assertEquals(
            "Password in scadenza",
            parameters.pretext
        )
        assertEquals(
            "Azienda Esempio",
            parameters.brandName
        )
    }

    @Test
    fun resolve_unsupportedDifficulty_returnsProfileNotFound() {
        val result =
            resolver.resolve(
                request(
                    scenario = Scenario.BANKING,
                    difficulty = Difficulty.HARD
                )
            )

        assertTrue(
            result is PromptParameterResolutionResult.Failure
        )

        val failure =
            result as PromptParameterResolutionResult.Failure

        assertEquals(
            PromptParameterResolutionIssueCode.PROFILE_NOT_FOUND,
            failure.issues.single().code
        )
    }

    @Test
    fun resolve_missingIndicator_returnsStructuredFailure() {
        val resolverWithMissingIndicator =
            DeterministicPromptParameterResolver(
                profileCatalog =
                    FrozenRuntimePromptProfileCatalog,
                libraryRepository =
                    TestLibraryRepository(
                        indicators = emptyList()
                    )
            )

        val result =
            resolverWithMissingIndicator.resolve(
                request(
                    scenario = Scenario.BANKING
                )
            )

        assertTrue(
            result is PromptParameterResolutionResult.Failure
        )

        val failure =
            result as PromptParameterResolutionResult.Failure

        assertTrue(
            failure.issues.all { issue ->
                issue.code ==
                        PromptParameterResolutionIssueCode
                            .INDICATOR_NOT_FOUND
            }
        )

        assertEquals(3, failure.issues.size)
    }

    @Test
    fun resolve_blankConfigurationId_returnsValidationFailure() {
        val result =
            resolver.resolve(
                request(
                    scenario = Scenario.BANKING,
                    configurationId = " "
                )
            )

        val failure =
            result as PromptParameterResolutionResult.Failure

        assertEquals(
            PromptParameterResolutionIssueCode
                .MISSING_CONFIGURATION_ID,
            failure.issues.single().code
        )
    }

    private fun request(
        scenario: Scenario,
        difficulty: Difficulty = Difficulty.MEDIUM,
        length: ExerciseLength = ExerciseLength.MEDIUM,
        configurationId: String = "configuration-1"
    ): PromptParameterResolutionRequest {
        return PromptParameterResolutionRequest(
            configurationId = configurationId,
            userConfiguration =
                UserConfiguration(
                    scenario = scenario,
                    difficulty = difficulty,
                    length = length
                ),
            language = "it"
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
            return indicators
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