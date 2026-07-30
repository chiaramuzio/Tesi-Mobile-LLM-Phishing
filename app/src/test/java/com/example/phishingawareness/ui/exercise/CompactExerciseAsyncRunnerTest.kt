package com.example.phishingawareness.ui.exercise

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.repository.LibraryRepository
import com.example.phishingawareness.domain.usecase.BuildQuizOptionsUseCase
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import com.example.phishingawareness.domain.usecase.CompactLocalEmailGenerator
import com.example.phishingawareness.domain.usecase.GenerateCompactExerciseUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

class CompactExerciseAsyncRunnerTest {

    @Test
    fun state_beforeStart_isIdle() {
        val runner =
            runner(
                generator =
                    StubCompactExerciseGenerator(
                        result =
                            successfulResult()
                    )
            )

        assertSame(
            CompactExerciseUiState.Idle,
            runner.state
        )

        assertFalse(
            runner.isRunning()
        )
    }

    @Test
    fun start_beforeTaskExecution_publishesLoading() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val observedStates =
            mutableListOf<CompactExerciseUiState>()

        val runner =
            CompactExerciseAsyncRunner(
                generator = generator,
                executor = executor,
                stateObserver = observedStates::add
            )

        val started =
            runner.start(
                request = validRequest()
            )

        assertTrue(started)
        assertTrue(runner.isRunning())

        assertSame(
            CompactExerciseUiState.Loading,
            runner.state
        )

        assertEquals(
            0,
            generator.invocationCount
        )

        assertEquals(
            listOf(
                CompactExerciseUiState.Loading
            ),
            observedStates
        )
    }

    @Test
    fun executeQueuedTask_publishesSuccess() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val runner =
            CompactExerciseAsyncRunner(
                generator = generator,
                executor = executor
            )

        runner.start(
            request = validRequest()
        )

        executor.runNext()

        assertTrue(
            runner.state is
                    CompactExerciseUiState.Success
        )

        assertFalse(
            runner.isRunning()
        )

        assertEquals(
            1,
            generator.invocationCount
        )

        val success =
            runner.state as
                    CompactExerciseUiState.Success

        assertEquals(
            6,
            success.result.exercise
                .quizOptions.size
        )
    }

    @Test
    fun generationFailure_preservesTypedFailure() {
        val expectedFailure =
            CompactExerciseGenerationResult
                .Failure
                .QuizBuilding(
                    details =
                        "Distrattori insufficienti"
                )

        val executor =
            QueuedExecutor()

        val runner =
            runner(
                generator =
                    StubCompactExerciseGenerator(
                        result = expectedFailure
                    ),
                executor = executor
            )

        runner.start(
            request = validRequest()
        )

        executor.runNext()

        val error =
            runner.state as
                    CompactExerciseUiState.Error

        val generationError =
            error.cause as
                    CompactExerciseUiError.Generation

        assertEquals(
            expectedFailure,
            generationError.failure
        )
    }

    @Test
    fun start_whileGenerationIsRunning_isRejected() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val runner =
            runner(
                generator = generator,
                executor = executor
            )

        val firstStart =
            runner.start(
                request = validRequest()
            )

        val secondStart =
            runner.start(
                request = validRequest()
            )

        assertTrue(firstStart)
        assertFalse(secondStart)

        assertEquals(
            1,
            executor.queuedTaskCount
        )

        executor.runNext()

        assertEquals(
            1,
            generator.invocationCount
        )
    }

    @Test
    fun unexpectedGeneratorException_publishesUnexpectedError() {
        val executor =
            QueuedExecutor()

        val generator =
            ThrowingCompactExerciseGenerator(
                exception =
                    IllegalStateException(
                        "errore inatteso"
                    )
            )

        val runner =
            runner(
                generator = generator,
                executor = executor
            )

        runner.start(
            request = validRequest()
        )

        executor.runNext()

        val error =
            runner.state as
                    CompactExerciseUiState.Error

        val unexpected =
            error.cause as
                    CompactExerciseUiError.Unexpected

        assertEquals(
            "errore inatteso",
            unexpected.details
        )

        assertFalse(
            runner.isRunning()
        )
    }

    @Test
    fun executorRejection_publishesUnexpectedError() {
        val runner =
            runner(
                generator =
                    StubCompactExerciseGenerator(
                        result =
                            successfulResult()
                    ),
                executor =
                    Executor {
                        throw RejectedExecutionException(
                            "executor rifiutato"
                        )
                    }
            )

        val started =
            runner.start(
                request = validRequest()
            )

        assertFalse(started)

        val error =
            runner.state as
                    CompactExerciseUiState.Error

        val unexpected =
            error.cause as
                    CompactExerciseUiError.Unexpected

        assertEquals(
            "executor rifiutato",
            unexpected.details
        )

        assertFalse(
            runner.isRunning()
        )
    }

    private fun runner(
        generator: CompactExerciseGenerator,
        executor: Executor =
            QueuedExecutor()
    ): CompactExerciseAsyncRunner {
        return CompactExerciseAsyncRunner(
            generator = generator,
            executor = executor
        )
    }

    private fun validRequest():
            GenerationRequest {
        return GenerationRequest(
            scenarioId = "ACCOUNT_IT",
            difficulty = "MEDIUM",
            length = "MEDIUM"
        )
    }

    private fun successfulResult():
            CompactExerciseGenerationResult.Success {
        val useCase =
            GenerateCompactExerciseUseCase(
                compactLocalEmailGenerator =
                    SuccessfulCompactLocalEmailGenerator(),
                buildQuizOptionsUseCase =
                    BuildQuizOptionsUseCase(
                        libraryRepository =
                            StubLibraryRepository()
                    )
            )

        return useCase(
            request = validRequest()
        ) as CompactExerciseGenerationResult.Success
    }

    private class StubCompactExerciseGenerator(
        private val result:
        CompactExerciseGenerationResult
    ) : CompactExerciseGenerator {

        var invocationCount: Int = 0
            private set

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            invocationCount += 1
            return result
        }
    }

    private class ThrowingCompactExerciseGenerator(
        private val exception:
        RuntimeException
    ) : CompactExerciseGenerator {

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            throw exception
        }
    }

    private class SuccessfulCompactLocalEmailGenerator :
        CompactLocalEmailGenerator {

        override fun invoke(
            request: GenerationRequest,
            options: LocalEmailGenerationOptions
        ): LocalEmailGenerationResult {
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
                            setOf(
                                "URGENCY_PRESSURE"
                            )
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
                        promptSha256 =
                            "compact-hash"
                    ),
                executionMetadata =
                    LocalModelExecutionMetadata(
                        promptSha256 =
                            "compact-hash",
                        seed = 101,
                        generatedCharacterCount = 800
                    )
            )
        }
    }

    private class QueuedExecutor :
        Executor {

        private val tasks =
            ArrayDeque<Runnable>()

        val queuedTaskCount: Int
            get() = tasks.size

        override fun execute(
            command: Runnable
        ) {
            tasks.addLast(command)
        }

        fun runNext() {
            tasks.removeFirst().run()
        }
    }

    private class StubLibraryRepository :
        LibraryRepository {

        override fun getManifest():
                LibraryManifest {
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
                listOf(
                    accountItIndicator()
                )
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
                    indicator.promptId ==
                            promptId
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
            return (1..5)
                .map { index ->
                    DistractorDefinition(
                        id =
                            "DISTRACTOR_$index",
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