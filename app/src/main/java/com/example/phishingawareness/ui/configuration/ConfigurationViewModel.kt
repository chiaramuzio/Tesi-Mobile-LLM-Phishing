package com.example.phishingawareness.ui.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phishingawareness.R
import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.model.UserConfiguration
import androidx.lifecycle.LiveData
import com.example.phishingawareness.domain.usecase.GetEnabledScenariosUseCase

class ConfigurationViewModel(
    private val getEnabledScenariosUseCase: GetEnabledScenariosUseCase
) : ViewModel() {
    private val _libraryUiState =
        MutableLiveData<LibraryUiState>(
            LibraryUiState.Loading
        )

    val libraryUiState: LiveData<LibraryUiState>
        get() = _libraryUiState

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        _libraryUiState.value = LibraryUiState.Loading

        try {
            val scenarios =
                getEnabledScenariosUseCase()

            if (scenarios.isEmpty()) {
                _libraryUiState.value =
                    LibraryUiState.Error(
                        message = "La libreria non contiene scenari attivi."
                    )

                return
            }

            _libraryUiState.value =
                LibraryUiState.Success(
                    scenarios = scenarios
                )
        } catch (exception: Exception) {
            _libraryUiState.value =
                LibraryUiState.Error(
                    message = exception.message
                        ?: "Errore durante il caricamento della libreria."
                )
        }
    }

    private val _selectedScenarioId =
        MutableLiveData("BANKING")

    val selectedScenarioId: LiveData<String>
        get() = _selectedScenarioId

    fun selectScenario(scenarioId: String) {
        _selectedScenarioId.value = scenarioId
    }

    val difficultyCheckedId = MutableLiveData(
        R.id.mediumRadioButton
    )

    val lengthCheckedId = MutableLiveData(
        R.id.mediumLengthRadioButton
    )

    fun getUserConfiguration(): UserConfiguration {
        return UserConfiguration(
            scenario = resolveScenario(),
            difficulty = resolveDifficulty(),
            length = resolveLength()
        )
    }

    private fun resolveScenario(): Scenario {
        return when (_selectedScenarioId.value) {
            "ACCOUNT_IT" -> Scenario.ACCOUNT_IT
            else -> Scenario.BANKING
        }
    }

    private fun resolveDifficulty(): Difficulty {
        return when (difficultyCheckedId.value) {
            R.id.easyRadioButton -> Difficulty.EASY
            R.id.hardRadioButton -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        }
    }

    private fun resolveLength(): ExerciseLength {
        return when (lengthCheckedId.value) {
            R.id.shortRadioButton -> ExerciseLength.SHORT
            R.id.longRadioButton -> ExerciseLength.LONG
            else -> ExerciseLength.MEDIUM
        }
    }
}