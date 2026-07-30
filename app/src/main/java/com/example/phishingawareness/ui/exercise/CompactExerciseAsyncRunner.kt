package com.example.phishingawareness.ui.exercise

import com.example.phishingawareness.domain.model.CompactExerciseGenerationResult
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.usecase.CompactExerciseGenerator
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class CompactExerciseAsyncRunner(
    private val generator:
    CompactExerciseGenerator,
    private val executor: Executor,
    private val stateObserver:
        (CompactExerciseUiState) -> Unit = {}
) {

    private val running =
        AtomicBoolean(false)

    @Volatile
    private var currentState:
            CompactExerciseUiState =
        CompactExerciseUiState.Idle

    val state: CompactExerciseUiState
        get() = currentState

    fun isRunning(): Boolean {
        return running.get()
    }

    fun start(
        request: GenerationRequest
    ): Boolean {
        if (
            !running.compareAndSet(
                false,
                true
            )
        ) {
            return false
        }

        publish(
            CompactExerciseUiState.Loading
        )

        return try {
            executor.execute {
                val finalState =
                    generateState(
                        request = request
                    )

                running.set(false)
                publish(finalState)
            }

            true
        } catch (
            exception: RuntimeException
        ) {
            running.set(false)

            publish(
                CompactExerciseUiState.Error(
                    cause =
                        CompactExerciseUiError
                            .Unexpected(
                                details =
                                    failureDetails(
                                        exception
                                    )
                            )
                )
            )

            false
        }
    }

    private fun generateState(
        request: GenerationRequest
    ): CompactExerciseUiState {
        return try {
            when (
                val result =
                    generator.generate(
                        request = request
                    )
            ) {
                is CompactExerciseGenerationResult.Success ->
                    CompactExerciseUiState.Success(
                        result = result
                    )

                is CompactExerciseGenerationResult.Failure ->
                    CompactExerciseUiState.Error(
                        cause =
                            CompactExerciseUiError
                                .Generation(
                                    failure = result
                                )
                    )
            }
        } catch (
            exception: RuntimeException
        ) {
            CompactExerciseUiState.Error(
                cause =
                    CompactExerciseUiError
                        .Unexpected(
                            details =
                                failureDetails(
                                    exception
                                )
                        )
            )
        }
    }

    private fun publish(
        newState: CompactExerciseUiState
    ) {
        currentState = newState
        stateObserver(newState)
    }

    private fun failureDetails(
        exception: RuntimeException
    ): String {
        return exception.message
            ?.takeIf { message ->
                message.isNotBlank()
            }
            ?: exception.javaClass.simpleName
    }
}