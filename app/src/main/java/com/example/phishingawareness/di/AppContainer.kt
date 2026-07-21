package com.example.phishingawareness.di

import android.content.Context
import com.example.phishingawareness.data.local.LibraryAssetDataSource
import com.example.phishingawareness.data.repository.AssetLibraryRepository
import com.example.phishingawareness.domain.repository.LibraryRepository
import com.example.phishingawareness.data.repository.FakeExerciseGenerationRepository
import com.example.phishingawareness.domain.repository.ExerciseGenerationRepository

class AppContainer(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val libraryAssetDataSource: LibraryAssetDataSource by lazy {
        LibraryAssetDataSource(
            context = applicationContext
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        AssetLibraryRepository(
            dataSource = libraryAssetDataSource
        )
    }

    val exerciseGenerationRepository:
            ExerciseGenerationRepository by lazy {
        FakeExerciseGenerationRepository()
    }
}