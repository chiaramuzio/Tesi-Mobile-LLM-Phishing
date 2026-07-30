package com.example.phishingawareness.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.ui.exercise.CompactExerciseViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContainerCompactWiringTest {

    @Test
    fun createFactory_doesNotStartNativeGeneration() {
        val context =
            ApplicationProvider
                .getApplicationContext<Context>()

        val container =
            AppContainer(
                context = context
            )

        val factory =
            container
                .createCompactExerciseViewModelFactory(
                    generationRequest =
                        GenerationRequest(
                            scenarioId =
                                "ACCOUNT_IT",
                            difficulty =
                                "MEDIUM",
                            length =
                                "MEDIUM"
                        )
                )

        val viewModel =
            factory.create(
                CompactExerciseViewModel::class.java
            )

        assertNotNull(factory)
        assertNotNull(viewModel)

        assertTrue(
            viewModel.uiState.value == null ||
                    viewModel.uiState.value
                        ?.javaClass
                        ?.simpleName == "Idle"
        )

        assertTrue(
            !viewModel.isGenerationRunning()
        )
    }
}
