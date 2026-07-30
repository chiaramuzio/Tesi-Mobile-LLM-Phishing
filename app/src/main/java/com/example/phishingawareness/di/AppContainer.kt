package com.example.phishingawareness.di

import android.content.Context
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.data.repository.FakeExerciseGenerationRepository
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.modelruntime.CompactRuntimeLifecycle
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository
import com.example.phishingawareness.domain.repository.LibraryRepository
import com.example.phishingawareness.domain.usecase.BuildCompactRuntimePromptUseCase
import com.example.phishingawareness.domain.usecase.BuildQuizOptionsUseCase
import com.example.phishingawareness.domain.usecase.BuildRuntimePromptUseCase
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import com.example.phishingawareness.domain.usecase.GenerateCompactExerciseUseCase
import com.example.phishingawareness.domain.usecase.GenerateCompactLocalEmailUseCase
import com.example.phishingawareness.domain.usecase.PreparedCompactExerciseGenerator
import com.example.phishingawareness.generation.model.AndroidCompactRuntimeLifecycle
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.output.DeterministicCompactModelOutputParser
import com.example.phishingawareness.generation.output.DeterministicCompactParsedEmailMapper
import com.example.phishingawareness.generation.prompt.DeterministicCompactRuntimePromptSectionResolver
import com.example.phishingawareness.generation.prompt.DeterministicPromptBuilder
import com.example.phishingawareness.generation.prompt.DeterministicPromptParameterResolver
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptGenerationOrchestrator
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptSectionResolver
import com.example.phishingawareness.generation.prompt.FrozenRuntimePromptProfileCatalog
import com.example.phishingawareness.generation.runtime.DeterministicLocalModelSession
import com.example.phishingawareness.generation.runtime.NativeLocalGenerationRuntime
import com.example.phishingawareness.generation.runtime.RuntimeLocalModelExecutor
import com.example.phishingawareness.ui.exercise.CompactExerciseViewModelFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppContainer(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val libraryAssetDataSource:
            LibraryAssetDataSource by lazy {
        LibraryAssetDataSource(
            context = applicationContext
        )
    }

    val libraryRepository:
            LibraryRepository by lazy {
        AssetLibraryRepository(
            dataSource = libraryAssetDataSource
        )
    }

    /*
     * Pipeline storica, mantenuta invariata.
     */
    private val runtimePromptGenerationOrchestrator:
            RuntimePromptGenerationOrchestrator by lazy {
        DeterministicRuntimePromptGenerationOrchestrator(
            parameterResolver =
                DeterministicPromptParameterResolver(
                    profileCatalog =
                        FrozenRuntimePromptProfileCatalog,
                    libraryRepository =
                        libraryRepository
                ),
            sectionResolver =
                DeterministicRuntimePromptSectionResolver(),
            promptBuilder =
                DeterministicPromptBuilder()
        )
    }

    val buildRuntimePromptUseCase:
            BuildRuntimePromptUseCase by lazy {
        BuildRuntimePromptUseCase(
            orchestrator =
                runtimePromptGenerationOrchestrator,
            libraryRepository =
                libraryRepository
        )
    }

    /*
     * Il flusso attualmente usato dalla schermata resta fake.
     */
    val exerciseGenerationRepository:
            ExerciseGenerationRepository by lazy {
        FakeExerciseGenerationRepository()
    }

    /*
     * Pipeline compatta reale.
     *
     * Tutte le dipendenze restano lazy:
     * creare AppContainer non carica il modello e non crea il context.
     */
    private val compactRuntimePromptGenerationOrchestrator:
            RuntimePromptGenerationOrchestrator by lazy {
        DeterministicRuntimePromptGenerationOrchestrator(
            parameterResolver =
                DeterministicPromptParameterResolver(
                    profileCatalog =
                        FrozenRuntimePromptProfileCatalog,
                    libraryRepository =
                        libraryRepository
                ),
            sectionResolver =
                DeterministicCompactRuntimePromptSectionResolver(),
            promptBuilder =
                DeterministicPromptBuilder()
        )
    }

    private val buildCompactRuntimePromptUseCase:
            BuildCompactRuntimePromptUseCase by lazy {
        BuildCompactRuntimePromptUseCase(
            orchestrator =
                compactRuntimePromptGenerationOrchestrator,
            libraryRepository =
                libraryRepository
        )
    }

    private val localModelBootstrap:
            DefaultLocalModelBootstrap by lazy {
        DefaultLocalModelBootstrap(
            pathProvider =
                AndroidLocalModelPathProvider(
                    context = applicationContext
                ),
            session =
                DeterministicLocalModelSession()
        )
    }

    private val compactRuntimeLifecycle:
            CompactRuntimeLifecycle by lazy {
        AndroidCompactRuntimeLifecycle(
            bootstrap = localModelBootstrap
        )
    }

    private val localModelExecutor:
            LocalModelExecutor by lazy {
        RuntimeLocalModelExecutor(
            runtime =
                NativeLocalGenerationRuntime()
        )
    }

    private val generateCompactLocalEmailUseCase:
            GenerateCompactLocalEmailUseCase by lazy {
        GenerateCompactLocalEmailUseCase(
            buildCompactRuntimePromptUseCase =
                buildCompactRuntimePromptUseCase,
            localModelExecutor =
                localModelExecutor,
            compactModelOutputParser =
                DeterministicCompactModelOutputParser(
                    libraryRepository =
                        libraryRepository
                ),
            compactParsedEmailMapper =
                DeterministicCompactParsedEmailMapper()
        )
    }

    private val generateCompactExerciseUseCase:
            GenerateCompactExerciseUseCase by lazy {
        GenerateCompactExerciseUseCase(
            compactLocalEmailGenerator =
                generateCompactLocalEmailUseCase,
            buildQuizOptionsUseCase =
                BuildQuizOptionsUseCase(
                    libraryRepository =
                        libraryRepository
                )
        )
    }

    val compactExerciseGenerator:
            CompactExerciseGenerator by lazy {
        PreparedCompactExerciseGenerator(
            runtimeLifecycle =
                compactRuntimeLifecycle,
            delegate =
                generateCompactExerciseUseCase
        )
    }

    private val compactGenerationExecutor:
            ExecutorService by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(
                runnable,
                COMPACT_GENERATION_THREAD_NAME
            )
        }
    }

    fun createCompactExerciseViewModelFactory(
        generationRequest: GenerationRequest
    ): CompactExerciseViewModelFactory {
        return CompactExerciseViewModelFactory(
            generator =
                compactExerciseGenerator,
            executor =
                compactGenerationExecutor,
            generationRequest =
                generationRequest
        )
    }

    private companion object {

        const val COMPACT_GENERATION_THREAD_NAME =
            "compact-local-generation"
    }
}