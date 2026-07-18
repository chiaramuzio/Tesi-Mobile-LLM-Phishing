package com.example.phishingawareness.ui.configuration

import com.example.phishingawareness.domain.model.ScenarioDefinition

sealed class LibraryUiState {

    data object Loading : LibraryUiState()

    data class Success(
        val scenarios: List<ScenarioDefinition>
    ) : LibraryUiState()

    data class Error(
        val message: String
    ) : LibraryUiState()
}