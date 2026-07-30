package com.example.phishingawareness.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phishingawareness.PhishingAwarenessApplication
import com.example.phishingawareness.databinding.FragmentExerciseBinding
import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.Exercise
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.ui.exercise.adapter.QuizOptionAdapter

class ExerciseFragment : Fragment() {

    private var _binding:
            FragmentExerciseBinding? = null

    private val binding:
            FragmentExerciseBinding
        get() = _binding!!

    private val args:
            ExerciseFragmentArgs by navArgs()

    private val viewModel:
            CompactExerciseViewModel by viewModels {
        val application =
            requireActivity().application
                    as PhishingAwarenessApplication

        application.appContainer
            .createCompactExerciseViewModelFactory(
                generationRequest =
                    GenerationRequest(
                        scenarioId =
                            args.scenario,
                        difficulty =
                            args.difficulty,
                        length =
                            args.length
                    )
            )
    }

    private lateinit var quizOptionAdapter:
            QuizOptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentExerciseBinding.inflate(
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
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupConfigurationSummary()
        setupRecyclerView()
        setupButtons()
        observeUiState()
        observeExercise()
        observeQuizOptions()
        observeQuizResult()
        startGenerationWhenIdle()
    }

    private fun setupConfigurationSummary() {
        binding.configurationSummary.text =
            """
            Scenario: ${args.scenario}
            Difficoltà: ${args.difficulty}
            Lunghezza: ${args.length}
            """.trimIndent()
    }

    private fun setupRecyclerView() {
        quizOptionAdapter =
            QuizOptionAdapter {
                    optionId,
                    isChecked ->

                viewModel.setOptionSelected(
                    optionId = optionId,
                    isSelected = isChecked
                )
            }

        binding.quizRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.quizRecyclerView.adapter =
            quizOptionAdapter
    }

    private fun setupButtons() {
        binding.showQuizButton
            .setOnClickListener {
                binding.quizGroup.isVisible =
                    true

                binding.showQuizButton.isVisible =
                    false
            }

        binding.submitQuizButton
            .setOnClickListener {
                viewModel.submitQuiz()
            }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                CompactExerciseUiState.Idle ->
                    renderIdleState()

                CompactExerciseUiState.Loading ->
                    renderLoadingState()

                is CompactExerciseUiState.Success ->
                    renderSuccessState(
                        exercise =
                            state.result.exercise
                    )

                is CompactExerciseUiState.Error ->
                    renderErrorState(
                        error = state.cause
                    )
            }
        }
    }

    private fun observeExercise() {
        viewModel.exercise.observe(
            viewLifecycleOwner
        ) { exercise ->
            if (exercise != null) {
                renderExercise(exercise)
            }
        }
    }

    private fun observeQuizOptions() {
        viewModel.exercise.observe(
            viewLifecycleOwner
        ) {
            updateQuizAdapter()
        }

        viewModel.selectedOptionIds.observe(
            viewLifecycleOwner
        ) {
            updateQuizAdapter()
        }
    }

    private fun updateQuizAdapter() {
        val exercise =
            viewModel.exercise.value

        if (exercise == null) {
            quizOptionAdapter.submitData(
                newOptions = emptyList(),
                newSelectedOptionIds =
                    emptySet()
            )

            return
        }

        quizOptionAdapter.submitData(
            newOptions =
                exercise.quizOptions,
            newSelectedOptionIds =
                viewModel.selectedOptionIds
                    .value
                    .orEmpty()
        )
    }

    private fun observeQuizResult() {
        viewModel.quizResult.observe(
            viewLifecycleOwner
        ) { result ->
            val hasResult =
                result != null

            binding.feedbackCorrect.isVisible =
                hasResult

            binding.feedbackIncorrect.isVisible =
                hasResult

            if (result != null) {
                binding.feedbackCorrect.text =
                    "Hai individuato " +
                            "${result.correctSelected} " +
                            "indicatori corretti su " +
                            "${result.totalCorrect}."

                binding.feedbackIncorrect.text =
                    "Hai selezionato " +
                            "${result.incorrectSelected} " +
                            "opzioni non presenti nella mail."
            }
        }
    }

    private fun startGenerationWhenIdle() {
        if (
            viewModel.uiState.value ===
            CompactExerciseUiState.Idle
        ) {
            viewModel.startGeneration()
        }
    }

    private fun renderIdleState() {
        hideExerciseActions()
    }

    private fun renderLoadingState() {
        binding.senderName.text =
            "Generazione in corso"

        binding.senderAddress.text = ""
        binding.emailSubject.text = ""

        binding.emailBody.text =
            "Il modello locale sta preparando e " +
                    "generando l’esercizio. " +
                    "L’operazione può richiedere diversi minuti."

        hideExerciseActions()

        quizOptionAdapter.submitData(
            newOptions = emptyList(),
            newSelectedOptionIds =
                emptySet()
        )
    }

    private fun renderSuccessState(
        exercise: Exercise
    ) {
        renderExercise(exercise)

        binding.showQuizButton.isVisible =
            true

        binding.showQuizButton.isEnabled =
            true

        binding.quizGroup.isVisible =
            false
    }

    private fun renderExercise(
        exercise: Exercise
    ) {
        binding.senderName.text =
            exercise.email.senderName

        binding.senderAddress.text =
            exercise.email.senderAddress

        binding.emailSubject.text =
            exercise.email.subject

        binding.emailBody.text =
            exercise.email.body
    }

    private fun renderErrorState(
        error: CompactExerciseUiError
    ) {
        binding.senderName.text =
            "Generazione non riuscita"

        binding.senderAddress.text = ""
        binding.emailSubject.text = ""

        binding.emailBody.text =
            buildErrorMessage(error)

        hideExerciseActions()

        quizOptionAdapter.submitData(
            newOptions = emptyList(),
            newSelectedOptionIds =
                emptySet()
        )
    }

    private fun hideExerciseActions() {
        binding.showQuizButton.isVisible =
            false

        binding.quizGroup.isVisible =
            false

        binding.feedbackCorrect.isVisible =
            false

        binding.feedbackIncorrect.isVisible =
            false
    }

    private fun buildErrorMessage(
        error: CompactExerciseUiError
    ): String {
        val details =
            when (error) {
                is CompactExerciseUiError.Generation ->
                    generationFailureDetails(
                        error.failure
                    )

                is CompactExerciseUiError.Unexpected ->
                    error.details
            }

        return buildString {
            append(
                "Non è stato possibile generare " +
                        "l’esercizio."
            )

            if (details.isNotBlank()) {
                append("\n\nDettagli: ")
                append(details)
            }
        }
    }

    private fun generationFailureDetails(
        failure:
        CompactExerciseGenerationResult.Failure
    ): String {
        return when (failure) {
            is CompactExerciseGenerationResult
            .Failure
            .LocalEmailGeneration ->
                "${failure.stage}: " +
                        failure.details

            is CompactExerciseGenerationResult
            .Failure
            .QuizBuilding ->
                failure.details

            is CompactExerciseGenerationResult
            .Failure
            .RuntimeLifecycle ->
                failure.details
        }
    }

    override fun onDestroyView() {
        binding.quizRecyclerView.adapter =
            null

        _binding = null

        super.onDestroyView()
    }
}