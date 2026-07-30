package com.example.phishingawareness.ui.exercise

import androidx.lifecycle.ViewModel
import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executor

class CompactExerciseViewModelFactoryTest {

    @Test
    fun create_supportedViewModel_returnsCompactExerciseViewModel() {
        val generator =
            RecordingCompactExerciseGenerator()

        val factory =
            CompactExerciseViewModelFactory(
                generator = generator,
                executor =
                    Executor { command ->
                        command.run()
                    },
                generationRequest =
                    validRequest()
            )

        val viewModel =
            factory.create(
                CompactExerciseViewModel::class.java
            )

        assertTrue(
            viewModel is CompactExerciseViewModel
        )

        assertEquals(
            0,
            generator.invocationCount
        )
    }

    @Test
    fun create_unsupportedViewModel_throwsIllegalArgumentException() {
        val factory =
            CompactExerciseViewModelFactory(
                generator =
                    RecordingCompactExerciseGenerator(),
                executor =
                    Executor { command ->
                        command.run()
                    },
                generationRequest =
                    validRequest()
            )

        val exception =
            try {
                factory.create(
                    UnsupportedViewModel::class.java
                )

                null
            } catch (
                caught: IllegalArgumentException
            ) {
                caught
            }

        requireNotNull(exception)

        assertTrue(
            exception.message.orEmpty()
                .contains(
                    UnsupportedViewModel::class.java.name
                )
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

    private class RecordingCompactExerciseGenerator :
        CompactExerciseGenerator {

        var invocationCount: Int = 0
            private set

        override fun generate(
            request: GenerationRequest
        ): CompactExerciseGenerationResult {
            invocationCount += 1

            throw AssertionError(
                "Il generatore non deve essere eseguito " +
                        "durante la creazione del ViewModel."
            )
        }
    }

    private class UnsupportedViewModel :
        ViewModel()
}