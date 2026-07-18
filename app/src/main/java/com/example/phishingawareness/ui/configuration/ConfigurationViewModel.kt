package com.example.phishingawareness.ui.configuration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phishingawareness.R
import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.model.UserConfiguration

class ConfigurationViewModel : ViewModel() {

    val scenarioCheckedId = MutableLiveData(
        R.id.bankingRadioButton
    )

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
        return when (scenarioCheckedId.value) {
            R.id.accountItRadioButton -> Scenario.ACCOUNT_IT
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