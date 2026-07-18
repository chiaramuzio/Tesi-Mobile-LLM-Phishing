package com.example.phishingawareness.ui.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phishingawareness.domain.usecase.GetEnabledScenariosUseCase

class ConfigurationViewModelFactory(
    private val getEnabledScenariosUseCase:
    GetEnabledScenariosUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                ConfigurationViewModel::class.java
            )
        ) {
            return ConfigurationViewModel(
                getEnabledScenariosUseCase =
                    getEnabledScenariosUseCase
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel non supportato: ${modelClass.name}"
        )
    }
}