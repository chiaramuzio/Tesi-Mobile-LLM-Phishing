package com.example.phishingawareness.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.phishingawareness.databinding.FragmentConfigurationBinding
import com.example.phishingawareness.PhishingAwarenessApplication
import com.example.phishingawareness.domain.usecase.GetEnabledScenariosUseCase
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.phishingawareness.domain.model.ScenarioDefinition

class ConfigurationFragment : Fragment() {
    private val scenarioIdsByViewId =
        mutableMapOf<Int, String>()

    private var _binding: FragmentConfigurationBinding? = null

    private val binding: FragmentConfigurationBinding
        get() = _binding!!

    private val viewModel: ConfigurationViewModel by viewModels {
        val application =
            requireActivity().application
                    as PhishingAwarenessApplication

        val repository =
            application.appContainer.libraryRepository

        val useCase =
            GetEnabledScenariosUseCase(
                libraryRepository = repository
            )

        ConfigurationViewModelFactory(
            getEnabledScenariosUseCase = useCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigurationBinding.inflate(
            inflater,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.libraryUiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                LibraryUiState.Loading -> {
                    binding.startExerciseButton.isEnabled = false
                }

                is LibraryUiState.Success -> {
                    setupScenarioRadioButtons(
                        scenarios = state.scenarios
                    )

                    binding.startExerciseButton.isEnabled = true
                }

                is LibraryUiState.Error -> {
                    binding.startExerciseButton.isEnabled = false
                }
            }
        }

        viewModel.selectedScenarioId.observe(
            viewLifecycleOwner
        ) {
            checkSelectedScenario()
        }

        binding.startExerciseButton.setOnClickListener {
            val configuration =
                viewModel.getUserConfiguration()

            val action =
                ConfigurationFragmentDirections
                    .actionConfigurationFragmentToExerciseFragment(
                        scenario = configuration.scenario.name,
                        difficulty = configuration.difficulty.name,
                        length = configuration.length.name
                    )

            findNavController().navigate(action)
        }
    }

    private fun setupScenarioRadioButtons(
        scenarios: List<ScenarioDefinition>
    ) {
        binding.scenarioRadioGroup.setOnCheckedChangeListener(null)
        binding.scenarioRadioGroup.removeAllViews()
        scenarioIdsByViewId.clear()

        scenarios.forEach { scenario ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = scenario.displayName
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
            }

            scenarioIdsByViewId[radioButton.id] = scenario.id
            binding.scenarioRadioGroup.addView(radioButton)
        }

        binding.scenarioRadioGroup.setOnCheckedChangeListener {
                _,
                checkedViewId ->

            val scenarioId =
                scenarioIdsByViewId[checkedViewId]
                    ?: return@setOnCheckedChangeListener

            viewModel.selectScenario(scenarioId)
        }

        checkSelectedScenario()
    }

    private fun checkSelectedScenario() {
        val selectedScenarioId =
            viewModel.selectedScenarioId.value ?: return

        val selectedViewId =
            scenarioIdsByViewId.entries
                .firstOrNull { entry ->
                    entry.value == selectedScenarioId
                }
                ?.key
                ?: return

        binding.scenarioRadioGroup.check(selectedViewId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}