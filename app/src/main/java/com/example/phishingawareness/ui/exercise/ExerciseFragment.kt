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
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.BuildQuizOptionsUseCase
import com.example.phishingawareness.domain.usecase.GenerateExerciseUseCase
import com.example.phishingawareness.ui.exercise.adapter.QuizOptionAdapter

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null

    private val binding: FragmentExerciseBinding
        get() = _binding!!

    private val args: ExerciseFragmentArgs by navArgs()

    private val viewModel: ExerciseViewModel by viewModels {
        val application =
            requireActivity().application
                    as PhishingAwarenessApplication

        val appContainer =
            application.appContainer

        val buildQuizOptionsUseCase =
            BuildQuizOptionsUseCase(
                libraryRepository =
                    appContainer.libraryRepository
            )

        val generateExerciseUseCase =
            GenerateExerciseUseCase(
                generationRepository =
                    appContainer.exerciseGenerationRepository,
                buildQuizOptionsUseCase =
                    buildQuizOptionsUseCase
            )

        val generationRequest =
            GenerationRequest(
                scenarioId = args.scenario,
                difficulty = args.difficulty,
                length = args.length
            )

        ExerciseViewModelFactory(
            generateExerciseUseCase =
                generateExerciseUseCase,
            generationRequest =
                generationRequest
        )
    }

    private lateinit var quizOptionAdapter: QuizOptionAdapter

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
        super.onViewCreated(view, savedInstanceState)

        setupConfigurationSummary()
        setupRecyclerView()
        setupButtons()
        observeUiState()
        observeSelectedOptions()
        observeQuizResult()
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
            LinearLayoutManager(requireContext())

        binding.quizRecyclerView.adapter =
            quizOptionAdapter
    }

    private fun setupButtons() {
        binding.showQuizButton.setOnClickListener {
            binding.quizGroup.visibility = View.VISIBLE
            binding.showQuizButton.visibility = View.GONE
        }

        binding.submitQuizButton.setOnClickListener {
            viewModel.submitQuiz()
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ExerciseUiState.Loading ->
                    showLoadingState()

                is ExerciseUiState.Success ->
                    showSuccessState(
                        state = state
                    )

                is ExerciseUiState.Error ->
                    showErrorState(
                        state = state
                    )
            }
        }
    }

    private fun showLoadingState() {
        binding.generationProgress.isVisible = true
        binding.generationStatus.isVisible = true
        binding.generationStatus.text =
            "Generazione locale in corso. L'operazione può richiedere alcuni minuti."

        setExerciseContentVisible(false)
        binding.quizGroup.visibility = View.GONE
    }

    private fun showSuccessState(
        state: ExerciseUiState.Success
    ) {
        val exercise = state.exercise

        binding.generationProgress.isVisible = false
        binding.generationStatus.isVisible = false

        setExerciseContentVisible(true)

        binding.senderName.text =
            exercise.email.senderName

        binding.senderAddress.text =
            exercise.email.senderAddress

        binding.emailSubject.text =
            exercise.email.subject

        binding.emailBody.text =
            exercise.email.body

        updateQuizAdapter()
    }

    private fun showErrorState(
        state: ExerciseUiState.Error
    ) {
        binding.generationProgress.isVisible = false
        binding.generationStatus.isVisible = true
        binding.generationStatus.text =
            "Generazione non riuscita: ${state.details}"

        setExerciseContentVisible(false)
        binding.quizGroup.visibility = View.GONE
    }

    private fun setExerciseContentVisible(
        visible: Boolean
    ) {
        binding.senderName.isVisible = visible
        binding.senderAddress.isVisible = visible
        binding.emailSubject.isVisible = visible
        binding.emailBody.isVisible = visible
        binding.showQuizButton.isVisible = visible
    }

    private fun observeSelectedOptions() {
        viewModel.selectedOptionIds.observe(
            viewLifecycleOwner
        ) {
            updateQuizAdapter()
        }
    }

    private fun updateQuizAdapter() {
        val exercise =
            (viewModel.uiState.value
                    as? ExerciseUiState.Success)
                ?.exercise
                ?: return

        val selectedOptionIds =
            viewModel.selectedOptionIds.value.orEmpty()

        quizOptionAdapter.submitData(
            newOptions = exercise.quizOptions,
            newSelectedOptionIds = selectedOptionIds
        )
    }

    private fun observeQuizResult() {
        viewModel.quizResult.observe(
            viewLifecycleOwner
        ) { result ->
            val hasResult = result != null

            binding.feedbackCorrect.isVisible = hasResult
            binding.feedbackIncorrect.isVisible = hasResult

            if (result != null) {
                binding.feedbackCorrect.text =
                    "Hai individuato ${result.correctSelected} " +
                            "indicatori corretti su ${result.totalCorrect}."

                binding.feedbackIncorrect.text =
                    "Hai selezionato ${result.incorrectSelected} " +
                            "opzioni non presenti nella mail."
            }
        }
    }

    override fun onDestroyView() {
        binding.quizRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
