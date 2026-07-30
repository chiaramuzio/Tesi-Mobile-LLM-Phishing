package com.example.phishingawareness.di

import android.content.Context
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.data.repository.LocalExerciseGenerationRepository
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository
import com.example.phishingawareness.domain.repository.LibraryRepository
import com.example.phishingawareness.domain.usecase.BuildRuntimePromptUseCase
import com.example.phishingawareness.domain.usecase.GenerateLocalEmailUseCase
import com.example.phishingawareness.generation.model.AndroidLocalModelPathProvider
import com.example.phishingawareness.generation.model.DefaultLocalModelBootstrap
import com.example.phishingawareness.generation.model.LocalModelBootstrap
import com.example.phishingawareness.generation.output.DeterministicModelOutputParser
import com.example.phishingawareness.generation.output.DeterministicParsedEmailMapper
import com.example.phishingawareness.generation.prompt.DeterministicPromptBuilder
import com.example.phishingawareness.generation.prompt.DeterministicPromptParameterResolver
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptGenerationOrchestrator
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptSectionResolver
import com.example.phishingawareness.generation.prompt.FrozenRuntimePromptProfileCatalog
import com.example.phishingawareness.generation.runtime.DeterministicLocalModelSession
import com.example.phishingawareness.generation.runtime.NativeLocalGenerationRuntime
import com.example.phishingawareness.generation.runtime.RuntimeLocalModelExecutor

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

    private val localModelBootstrap:
            LocalModelBootstrap by lazy {
        DefaultLocalModelBootstrap(
            pathProvider =
                AndroidLocalModelPathProvider(
                    context = applicationContext
                ),
            session =
                DeterministicLocalModelSession()
        )
    }

    private val localModelExecutor:
            LocalModelExecutor by lazy {
        RuntimeLocalModelExecutor(
            runtime =
                NativeLocalGenerationRuntime()
        )
    }

    private val generateLocalEmailUseCase:
            GenerateLocalEmailUseCase by lazy {
        GenerateLocalEmailUseCase(
            buildRuntimePromptUseCase =
                buildRuntimePromptUseCase,
            localModelExecutor =
                localModelExecutor,
            modelOutputParser =
                DeterministicModelOutputParser(
                    libraryRepository =
                        libraryRepository
                ),
            parsedEmailMapper =
                DeterministicParsedEmailMapper()
        )
    }

    val exerciseGenerationRepository:
            ExerciseGenerationRepository by lazy {
        LocalExerciseGenerationRepository(
            localModelBootstrap =
                localModelBootstrap,
            generateLocalEmailUseCase =
                generateLocalEmailUseCase
        )
    }
}