package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.LocalEmailGenerationFailureStage
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateCompactExerciseUseCaseTest {

    @Test
    fun invoke_successfulEmail_returnsExerciseAndMetadata() {
        val generator =
            StubCompactLocalEmailGenerator(
                result = successfulEmailResult()
            )

        val result =
            useCase(
                generator = generator
            )(
                request = validRequest()
            )

        assertTrue(
            result is
                    CompactExerciseGenerationResult.Success
        )

        val success =
            result as
                    CompactExerciseGenerationResult.Success

        assertEquals(
            "Supporto IT",
            success.exercise.email.senderName
        )

        assertEquals(
            "Aggiornamento password",
            success.exercise.email.subject
        )

        assertEquals(
            6,
            success.exercise.quizOptions.size
        )

        assertEquals(
            1,
            success.exercise.quizOptions.count {
                    option -> option.isCorrect
            }
        )

        assertEquals(
            "URGENCY_PRESSURE",
            success.exercise.quizOptions
                .first { option -> option.isCorrect }
                .id
        )

        assertEquals(
            "compact-hash",
            success.promptMetadata.promptSha256
        )

        assertEquals(
            101,
            success.executionMetadata.seed
        )
    }

    @Test
    fun invoke_defaultOptions_usesValidatedCompactConfiguration() {
        val generator =
            StubCompactLocalEmailGenerator(
                result = successfulEmailResult()
            )

        useCase(
            generator = generator
        )(
            request = validRequest()
        )

        val options =
            requireNotNull(
                generator.receivedOptions
            )

        assertEquals(101, options.seed)
        assertEquals(8192, options.contextSize)
        assertEquals(600, options.maxGeneratedTokens)
        assertEquals(0.4f, options.temperature)
        assertEquals(40, options.topK)
        assertEquals(0.90f, options.topP)
        assertEquals(0.05f, options.minP)
        assertEquals(1.05f, options.repeatPenalty)
    }

    @Test
    fun invoke_customOptions_forwardsOptionsUnchanged() {
        val generator =
            StubCompactLocalEmailGenerator(
                result = successfulEmailResult()
            )

        val customOptions =
            LocalEmailGenerationOptions(
                seed = 404,
                contextSize = 4096,
                maxGeneratedTokens = 320,
                temperature = 0.5f,
                topK = 20,
                topP = 0.80f,
                minP = 0.02f,
                repeatPenalty = 1.10f
            )

        useCase(
            generator = generator
        )(
            request = validRequest(),
            options = customOptions
        )

        assertEquals(
            customOptions,
            generator.receivedOptions
        )
    }

    @Test
    fun invoke_emailFailure_preservesStageAndDetails() {
        val generator =
            StubCompactLocalEmailGenerator(
                result =
                    LocalEmailGenerationResult.Failure(
                        stage =
                            LocalEmailGenerationFailureStage
                                .OUTPUT_PARSING,
                        details =
                            "MALFORMED_JSON[rawOutput]"
                    )
            )

        val result =
            useCase(
                generator = generator
            )(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .LocalEmailGeneration

        assertEquals(
            LocalEmailGenerationFailureStage
                .OUTPUT_PARSING,
            failure.stage
        )

        assertEquals(
            "MALFORMED_JSON[rawOutput]",
            failure.details
        )
    }

    @Test
    fun invoke_noObservableIndicator_returnsQuizBuildingFailure() {
        val generator =
            StubCompactLocalEmailGenerator(
                result =
                    successfulEmailResult(
                        presentIndicatorIds =
                            setOf("UNKNOWN_INDICATOR")
                    )
            )

        val result =
            useCase(
                generator = generator
            )(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .QuizBuilding

        assertTrue(
            failure.details.contains(
                "Non sono presenti indicatori osservabili"
            )
        )
    }

    @Test
    fun invoke_insufficientDistractors_returnsQuizBuildingFailure() {
        val result =
            GenerateCompactExerciseUseCase(
                compactLocalEmailGenerator =
                    StubCompactLocalEmailGenerator(
                        result = successfulEmailResult()
                    ),
                buildQuizOptionsUseCase =
                    BuildQuizOptionsUseCase(
                        libraryRepository =
                            StubLibraryRepository(
                                distractorCount = 4
                            )
                    )
            )(
                request = validRequest()
            )

        val failure =
            result as
                    CompactExerciseGenerationResult
                    .Failure
                    .QuizBuilding

        assertTrue(
            failure.details.contains(
                "Distrattori insufficienti"
            )
        )
    }

    private fun useCase(
        generator: CompactLocalEmailGenerator
    ): GenerateCompactExerciseUseCase {
        return GenerateCompactExerciseUseCase(
            compactLocalEmailGenerator = generator,
            buildQuizOptionsUseCase =
                BuildQuizOptionsUseCase(
                    libraryRepository =
                        StubLibraryRepository()
                )
        )
    }

    private fun validRequest(): GenerationRequest {
        return GenerationRequest(
            scenarioId = "ACCOUNT_IT",
            difficulty = "MEDIUM",
            length = "MEDIUM"
        )
    }

    private fun successfulEmailResult(
        presentIndicatorIds: Set<String> =
            setOf("URGENCY_PRESSURE")
    ): LocalEmailGenerationResult.Success {
        return LocalEmailGenerationResult.Success(
            email =
                GeneratedEmail(
                    senderName = "Supporto IT",
                    senderAddress =
                        "supporto@aziendaesempio.invalid",
                    subject =
                        "Aggiornamento password",
                    body =
                        "Corpo della simulazione",
                    presentIndicatorIds =
                        presentIndicatorIds
                ),
            promptMetadata =
                PromptMetadata(
                    buildContext =
                        PromptBuildContext(
                            builderVersion = "1",
                            templateId =
                                "RUNTIME_MODULAR_COMPACT",
                            templateVersion = "1",
                            libraryId =
                                "phishing-awareness-library",
                            libraryVersion = "0.3.0",
                            librarySchemaVersion = 2
                        ),
                    resolvedConfigurationId =
                        "ACCOUNT_IT_MEDIUM_MEDIUM",
                    promptSha256 = "compact-hash"
                ),
            executionMetadata =
                LocalModelExecutionMetadata(
                    promptSha256 = "compact-hash",
                    seed = 101,
                    generatedCharacterCount = 800
                )
        )
    }

    private class StubCompactLocalEmailGenerator(
        private val result:
        LocalEmailGenerationResult
    ) : CompactLocalEmailGenerator {

        var receivedRequest:
                GenerationRequest? = null
            private set

        var receivedOptions:
                LocalEmailGenerationOptions? = null
            private set

        override fun invoke(
            request: GenerationRequest,
            options: LocalEmailGenerationOptions
        ): LocalEmailGenerationResult {
            receivedRequest = request
            receivedOptions = options

            return result
        }
    }

    private class StubLibraryRepository(
        private val distractorCount: Int = 5
    ) : LibraryRepository {

        override fun getManifest(): LibraryManifest {
            return LibraryManifest(
                libraryId =
                    "phishing-awareness-library",
                version = "0.3.0",
                schemaVersion = 2,
                language = "it",
                activeScenarios =
                    listOf(
                        "BANKING",
                        "ACCOUNT_IT"
                    ),
                resources = emptyList()
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
            listOf(accountItIndicator())

        override fun getDistractors():
                List<DistractorDefinition> =
            accountItDistractors()

        override fun getIndicatorsForScenario(
            scenarioId: String
        ): List<IndicatorDefinition> {
            return if (
                scenarioId == "ACCOUNT_IT"
            ) {
                listOf(accountItIndicator())
            } else {
                emptyList()
            }
        }

        override fun getDistractorsForScenario(
            scenarioId: String
        ): List<DistractorDefinition> {
            return if (
                scenarioId == "ACCOUNT_IT"
            ) {
                accountItDistractors()
            } else {
                emptyList()
            }
        }

        override fun getIndicatorById(
            indicatorId: String
        ): IndicatorDefinition? {
            return accountItIndicator()
                .takeIf { indicator ->
                    indicator.id == indicatorId
                }
        }

        override fun getIndicatorByPromptId(
            promptId: String
        ): IndicatorDefinition? {
            return accountItIndicator()
                .takeIf { indicator ->
                    indicator.promptId == promptId
                }
        }

        private fun accountItIndicator():
                IndicatorDefinition {
            return IndicatorDefinition(
                id = "URGENCY_PRESSURE",
                promptId = "IND_URGENCY",
                displayName =
                    "Pressione temporale",
                description =
                    "Richiesta presentata come urgente.",
                observable = true,
                enabled = true,
                scenarios =
                    listOf("ACCOUNT_IT")
            )
        }

        private fun accountItDistractors():
                List<DistractorDefinition> {
            return (1..distractorCount)
                .map { index ->
                    DistractorDefinition(
                        id = "DISTRACTOR_$index",
                        displayName =
                            "Distrattore $index",
                        description =
                            "Opzione non osservata.",
                        enabled = true,
                        scenarios =
                            listOf("ACCOUNT_IT")
                    )
                }
        }
    }
}