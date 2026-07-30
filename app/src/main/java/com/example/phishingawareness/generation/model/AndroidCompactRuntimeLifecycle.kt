package com.example.phishingawareness.generation.model

import com.example.phishingawareness.domain.modelruntime.CompactRuntimeLifecycle
import com.example.phishingawareness.domain.modelruntime.CompactRuntimePreparationResult

class AndroidCompactRuntimeLifecycle private constructor(
    private val prepareAction:
        (Int) -> CompactRuntimePreparationResult,
    private val releaseAction:
        () -> Unit
) : CompactRuntimeLifecycle {

    constructor(
        bootstrap: DefaultLocalModelBootstrap
    ) : this(
        prepareAction = { contextSize ->
            bootstrap
                .prepare(
                    contextSize = contextSize
                )
                .toCompactPreparationResult()
        },
        releaseAction = {
            bootstrap.release()
            Unit
        }
    )

    internal constructor(
        prepareAction:
            (Int) -> CompactRuntimePreparationResult,
        releaseAction:
            () -> Unit,
        @Suppress("UNUSED_PARAMETER")
        testOnlyMarker: Unit = Unit
    ) : this(
        prepareAction = prepareAction,
        releaseAction = releaseAction
    )

    override fun prepare(
        contextSize: Int
    ): CompactRuntimePreparationResult {
        return prepareAction(contextSize)
    }

    override fun release() {
        releaseAction()
    }
}

private fun LocalModelBootstrapResult
        .toCompactPreparationResult():
        CompactRuntimePreparationResult {
    return when (this) {
        is LocalModelBootstrapResult.Ready ->
            CompactRuntimePreparationResult.Ready

        is LocalModelBootstrapResult.PathFailure ->
            CompactRuntimePreparationResult.Failure(
                details =
                    buildString {
                        append("PATH/")
                        append(failure.code.name)
                        append(": ")
                        append(failure.details)
                    }
            )

        is LocalModelBootstrapResult.SessionFailure ->
            CompactRuntimePreparationResult.Failure(
                details =
                    buildString {
                        append(failure.stage.name)
                        append("/")
                        append(failure.code.name)
                        append(": ")
                        append(failure.details)
                    }
            )
    }
}