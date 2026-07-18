package com.example.phishingawareness.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.phishingawareness.databinding.FragmentConfigurationBinding

class ConfigurationFragment : Fragment() {

    private var _binding: FragmentConfigurationBinding? = null

    private val binding: FragmentConfigurationBinding
        get() = _binding!!

    private val viewModel: ConfigurationViewModel by viewModels()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}