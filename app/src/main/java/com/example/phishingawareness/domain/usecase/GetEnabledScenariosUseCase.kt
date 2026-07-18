package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.data.repository.LibraryRepository
import com.example.phishingawareness.domain.model.ScenarioDefinition

class GetEnabledScenariosUseCase(
    private val libraryRepository: LibraryRepository
) {

    operator fun invoke(): List<ScenarioDefinition> {
        return libraryRepository.getEnabledScenarios()
    }
}