package com.example.phishingawareness.generation.model

import com.example.phishingawareness.generation.runtime.LocalModelSession
import com.example.phishingawareness.generation.runtime.LocalModelSessionResult

/**
 * Implementazione applicativa che collega la risoluzione
 * del file GGUF alla preparazione della sessione nativa.
 */
class DefaultLocalModelBootstrap(
    private val pathProvider: LocalModelPathProvider,
    private val session: LocalModelSession
) : LocalModelBootstrap {

    override fun prepare(
        contextSize: Int
    ): LocalModelBootstrapResult {
        val pathResult =
            pathProvider.resolve()

        val availableModel =
            when (pathResult) {
                is LocalModelPathResult.Available ->
                    pathResult

                is LocalModelPathResult.Unavailable ->
                    return LocalModelBootstrapResult
                        .PathFailure(
                            failure = pathResult
                        )
            }

        return when (
            val sessionResult =
                session.prepare(
                    modelPath =
                        availableModel.absolutePath,
                    contextSize =
                        contextSize
                )
        ) {
            is LocalModelSessionResult.Ready ->
                LocalModelBootstrapResult.Ready(
                    modelPath =
                        availableModel.absolutePath,
                    modelSizeBytes =
                        availableModel.sizeBytes,
                    session =
                        sessionResult
                )

            is LocalModelSessionResult.Failure ->
                LocalModelBootstrapResult
                    .SessionFailure(
                        failure = sessionResult
                    )

            LocalModelSessionResult.Released ->
                error(
                    "prepare() non può restituire Released."
                )
        }
    }

    override fun release():
            LocalModelSessionResult {
        return session.release()
    }

    override fun isReady(): Boolean {
        return session.isReady()
    }
}