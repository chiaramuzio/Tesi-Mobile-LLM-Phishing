package com.example.phishingawareness.ui.exercise

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationFailureStage
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.QuizOption
import com.example.phishingawareness.domain.model.SimulatedEmail
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.ArrayDeque
import java.util.concurrent.Executor

class CompactExerciseViewModelTest {

    @get:Rule
    val instantTaskExecutorRule =
        InstantTaskExecutorRule()

    @Test
    fun initialState_isIdle() {
        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    )
            )

        assertSame(
            CompactExerciseUiState.Idle,
            viewModel.uiState.value
        )

        assertFalse(
            viewModel.isGenerationRunning()
        )
    }

    @Test
    fun startGeneration_publishesLoadingBeforeExecution() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val viewModel =
            viewModel(
                generator = generator,
                executor = executor
            )

        val started =
            viewModel.startGeneration()

        assertTrue(started)

        assertSame(
            CompactExerciseUiState.Loading,
            viewModel.uiState.value
        )

        assertTrue(
            viewModel.isGenerationRunning()
        )

        assertEquals(
            0,
            generator.invocationCount
        )

        assertEquals(
            1,
            executor.queuedTaskCount
        )
    }

    @Test
    fun completedGeneration_publishesSuccess() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val viewModel =
            viewModel(
                generator = generator,
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        assertTrue(
            viewModel.uiState.value is
                    CompactExerciseUiState.Success
        )

        assertFalse(
            viewModel.isGenerationRunning()
        )

        assertEquals(
            1,
            generator.invocationCount
        )

        assertEquals(
            validRequest(),
            generator.receivedRequest
        )

        val success =
            viewModel.uiState.value as
                    CompactExerciseUiState.Success

        assertEquals(
            6,
            success.result.exercise
                .quizOptions.size
        )

        assertEquals(
            "Supporto IT",
            success.result.exercise
                .email.senderName
        )
    }

    @Test
    fun generationFailure_publishesTypedError() {
        val expectedFailure =
            CompactExerciseGenerationResult
                .Failure
                .LocalEmailGeneration(
                    stage =
                        LocalEmailGenerationFailureStage
                            .OUTPUT_PARSING,
                    details =
                        "MALFORMED_JSON[rawOutput]"
                )

        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = expectedFailure
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        val error =
            viewModel.uiState.value as
                    CompactExerciseUiState.Error

        val generationError =
            error.cause as
                    CompactExerciseUiError.Generation

        assertEquals(
            expectedFailure,
            generationError.failure
        )

        assertFalse(
            viewModel.isGenerationRunning()
        )
    }

    @Test
    fun secondStartWhileRunning_isRejected() {
        val executor =
            QueuedExecutor()

        val generator =
            StubCompactExerciseGenerator(
                result = successfulResult()
            )

        val viewModel =
            viewModel(
                generator = generator,
                executor = executor
            )

        val firstStart =
            viewModel.startGeneration()

        val secondStart =
            viewModel.startGeneration()

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

        val viewModel =
            viewModel(
                generator =
                    ThrowingCompactExerciseGenerator(
                        exception =
                            IllegalStateException(
                                "errore inatteso"
                            )
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        val error =
            viewModel.uiState.value as
                    CompactExerciseUiState.Error

        val unexpected =
            error.cause as
                    CompactExerciseUiError.Unexpected

        assertEquals(
            "errore inatteso",
            unexpected.details
        )

        assertFalse(
            viewModel.isGenerationRunning()
        )
    }

    @Test
    fun completedGeneration_exposesExerciseAndEmptyQuizState() {
        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        val exercise =
            requireNotNull(
                viewModel.exercise.value
            )

        assertEquals(
            "Supporto IT",
            exercise.email.senderName
        )

        assertEquals(
            6,
            exercise.quizOptions.size
        )

        assertEquals(
            emptySet<String>(),
            viewModel.selectedOptionIds.value
        )

        assertNull(
            viewModel.quizResult.value
        )
    }

    @Test
    fun setOptionSelected_updatesSelectionsAndClearsResult() {
        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        viewModel.setOptionSelected(
            optionId =
                "URGENCY_PRESSURE",
            isSelected = true
        )

        assertEquals(
            setOf("URGENCY_PRESSURE"),
            viewModel.selectedOptionIds.value
        )

        viewModel.submitQuiz()

        assertTrue(
            viewModel.quizResult.value != null
        )

        viewModel.setOptionSelected(
            optionId =
                "URGENCY_PRESSURE",
            isSelected = false
        )

        assertEquals(
            emptySet<String>(),
            viewModel.selectedOptionIds.value
        )

        assertNull(
            viewModel.quizResult.value
        )
    }

    @Test
    fun submitQuiz_countsCorrectAndIncorrectSelections() {
        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        viewModel.setOptionSelected(
            optionId =
                "URGENCY_PRESSURE",
            isSelected = true
        )

        viewModel.setOptionSelected(
            optionId =
                "DISTRACTOR_1",
            isSelected = true
        )

        viewModel.submitQuiz()

        val result =
            requireNotNull(
                viewModel.quizResult.value
            )

        assertEquals(
            1,
            result.correctSelected
        )

        assertEquals(
            1,
            result.totalCorrect
        )

        assertEquals(
            1,
            result.incorrectSelected
        )
    }

    @Test
    fun setOptionSelected_ignoresUnknownOptionId() {
        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        viewModel.setOptionSelected(
            optionId =
                "UNKNOWN_OPTION",
            isSelected = true
        )

        assertEquals(
            emptySet<String>(),
            viewModel.selectedOptionIds.value
        )

        assertNull(
            viewModel.quizResult.value
        )
    }

    @Test
    fun quizActionsBeforeGeneration_doNothing() {
        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    )
            )

        viewModel.setOptionSelected(
            optionId =
                "URGENCY_PRESSURE",
            isSelected = true
        )

        viewModel.submitQuiz()

        assertEquals(
            emptySet<String>(),
            viewModel.selectedOptionIds.value
        )

        assertNull(
            viewModel.quizResult.value
        )

        assertNull(
            viewModel.exercise.value
        )
    }

    @Test
    fun newGeneration_resetsExerciseSelectionsAndResultWhileLoading() {
        val executor =
            QueuedExecutor()

        val viewModel =
            viewModel(
                generator =
                    StubCompactExerciseGenerator(
                        result = successfulResult()
                    ),
                executor = executor
            )

        viewModel.startGeneration()
        executor.runNext()

        viewModel.setOptionSelected(
            optionId =
                "URGENCY_PRESSURE",
            isSelected = true
        )

        viewModel.submitQuiz()

        assertTrue(
            viewModel.exercise.value != null
        )

        assertTrue(
            viewModel.selectedOptionIds
                .value
                .orEmpty()
                .isNotEmpty()
        )

        assertTrue(
            viewModel.quizResult.value != null
        )

        val restarted =
            viewModel.startGeneration()

        assertTrue(restarted)

        assertSame(
            CompactExerciseUiState.Loading,
            viewModel.uiState.value
        )

        assertNull(
            viewModel.exercise.value
        )

        assertEquals(
            emptySet<String>(),
            viewModel.selectedOptionIds.value
        )

        assertNull(
            viewModel.quizResult.value
        )
    }

    private fun viewModel(
        generator: CompactExerciseGenerator,
        executor: Executor =
            QueuedExecutor()
    ): CompactExerciseViewModel {
        return CompactExerciseViewModel(
            generator = generator,
            executor = executor,
            generationRequest = validRequest()
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
        return CompactExerciseGenerationResult.Success(
            exercise =
                Exercise(
                    email =
                        SimulatedEmail(
                            senderName = "Supporto IT",
                            senderAddress =
                                "supporto@aziendaesempio.invalid",
                            subject =
                                "Aggiornamento password",
                            body =
                                "Corpo della simulazione"
                        ),
                    quizOptions =
                        listOf(
                            QuizOption(
                                id =
                                    "URGENCY_PRESSURE",
                                text =
                                    "Pressione temporale",
                                isCorrect = true
                            ),
                            QuizOption(
                                id = "DISTRACTOR_1",
                                text = "Distrattore 1",
                                isCorrect = false
                            ),
                            QuizOption(
                                id = "DISTRACTOR_2",
                                text = "Distrattore 2",
                                isCorrect = false
                            ),
                            QuizOption(
                                id = "DISTRACTOR_3",
                                text = "Distrattore 3",
                                isCorrect = false
                            ),
                            QuizOption(
                                id = "DISTRACTOR_4",
                                text = "Distrattore 4",
                                isCorrect = false
                            ),
                            QuizOption(
                                id = "DISTRACTOR_5",
                                text = "Distrattore 5",
                                isCorrect = false
                            )
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

    private class StubCompactExerciseGenerator(
        private val result:
        CompactExerciseGenerationResult
    ) : CompactExerciseGenerator {

        var invocationCount: Int = 0
            private set

        var receivedRequest:
                GenerationRequest? = null
            private set

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            invocationCount += 1
            receivedRequest = request

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
}