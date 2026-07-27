package com.example.phishingawareness.di

import android.content.Context
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.data.repository.FakeExerciseGenerationRepository
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository
import com.example.phishingawareness.domain.repository.LibraryRepository
import com.example.phishingawareness.domain.usecase.BuildRuntimePromptUseCase
import com.example.phishingawareness.generation.prompt.DeterministicPromptBuilder
import com.example.phishingawareness.generation.prompt.DeterministicPromptParameterResolver
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptGenerationOrchestrator
import com.example.phishingawareness.generation.prompt.DeterministicRuntimePromptSectionResolver
import com.example.phishingawareness.generation.prompt.FrozenRuntimePromptProfileCatalog
import com.example.phishingawareness.generation.prompt.Gemma1BCompactRuntimePromptSectionResolver

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
                Gemma1BCompactRuntimePromptSectionResolver(),
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

    val exerciseGenerationRepository:
            ExerciseGenerationRepository by lazy {
        FakeExerciseGenerationRepository()
    }
}