package com.example.phishingawareness.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.phishingawareness.R
import com.example.phishingawareness.databinding.FragmentConfigurationBinding

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null

    private val binding: FragmentConfigurationBinding
        get() = _binding!!

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

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.startExerciseButton.setOnClickListener {
            val scenario = resolveScenario()
            val difficulty = resolveDifficulty()
            val length = resolveLength()

            val action =
                ConfigurationFragmentDirections
                    .actionConfigurationFragmentToExerciseFragment(
                        scenario = scenario,
                        difficulty = difficulty,
                        length = length
                    )

            findNavController().navigate(action)
        }
    }

    private fun resolveScenario(): String {
        return when (binding.scenarioRadioGroup.checkedRadioButtonId) {
            R.id.accountItRadioButton -> "ACCOUNT_IT"
            else -> "BANKING"
        }
    }

    private fun resolveDifficulty(): String {
        return when (binding.difficultyRadioGroup.checkedRadioButtonId) {
            R.id.easyRadioButton -> "EASY"
            R.id.hardRadioButton -> "HARD"
            else -> "MEDIUM"
        }
    }

    private fun resolveLength(): String {
        return when (binding.lengthRadioGroup.checkedRadioButtonId) {
            R.id.shortRadioButton -> "SHORT"
            R.id.longRadioButton -> "LONG"
            else -> "MEDIUM"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}