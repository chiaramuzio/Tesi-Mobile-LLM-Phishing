package com.example.phishingawareness.generation.runtime

/**
 * Gestisce in modo deterministico il ciclo di vita del modello
 * e del context di inferenza.
 *
 * La sessione:
 * - carica il modello solo quando necessario;
 * - crea il context solo quando necessario;
 * - verifica la dimensione del context esistente;
 * - libera prima il context e poi il modello;
 * - non esegue generazioni.
 */
class DeterministicLocalModelSession(
    private val gateway: NativeModelSessionGateway =
        DefaultNativeModelSessionGateway
) : LocalModelSession {

    override fun prepare(
        modelPath: String,
        contextSize: Int
    ): LocalModelSessionResult {
        if (modelPath.isBlank()) {
            return failure(
                stage =
                    LocalModelSessionFailureStage
                        .REQUEST_VALIDATION,
                code =
                    LocalModelSessionFailureCode
                        .MODEL_PATH_EMPTY,
                details =
                    "Il percorso del modello non può essere vuoto."
            )
        }

        if (contextSize <= 0) {
            return failure(
                stage =
                    LocalModelSessionFailureStage
                        .REQUEST_VALIDATION,
                code =
                    LocalModelSessionFailureCode
                        .INVALID_CONTEXT_SIZE,
                details =
                    "La dimensione del context deve essere positiva."
            )
        }

        return try {
            prepareValidated(
                modelPath = modelPath,
                requestedContextSize = contextSize
            )
        } catch (exception: RuntimeException) {
            failure(
                stage =
                    LocalModelSessionFailureStage
                        .MODEL_LOADING,
                code =
                    LocalModelSessionFailureCode
                        .UNKNOWN_NATIVE_RESPONSE,
                details =
                    exception.message
                        ?: exception::class.java.simpleName
            )
        }
    }

    override fun release(): LocalModelSessionResult {
        return try {
            releaseSafely()
        } catch (exception: RuntimeException) {
            failure(
                stage =
                    LocalModelSessionFailureStage
                        .MODEL_RELEASE,
                code =
                    LocalModelSessionFailureCode
                        .UNKNOWN_NATIVE_RESPONSE,
                details =
                    exception.message
                        ?: exception::class.java.simpleName
            )
        }
    }

    override fun isReady(): Boolean {
        return try {
            gateway.isModelLoaded() &&
                    gateway.isContextReady()
        } catch (_: RuntimeException) {
            false
        }
    }

    private fun prepareValidated(
        modelPath: String,
        requestedContextSize: Int
    ): LocalModelSessionResult {
        val modelAlreadyLoaded =
            gateway.isModelLoaded()

        if (!modelAlreadyLoaded) {
            val loadResponse =
                gateway.loadModel(
                    modelPath = modelPath
                )

            if (loadResponse != MODEL_LOADED_RESPONSE) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .MODEL_LOADING,
                    code =
                        LocalModelSessionFailureCode
                            .MODEL_LOAD_FAILED,
                    details = loadResponse
                )
            }

            if (!gateway.isModelLoaded()) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .MODEL_LOADING,
                    code =
                        LocalModelSessionFailureCode
                            .MODEL_LOAD_FAILED,
                    details =
                        "Il runtime non conferma il caricamento del modello."
                )
            }
        }

        val contextAlreadyReady =
            gateway.isContextReady()

        if (contextAlreadyReady) {
            return readyWithExistingContext(
                modelAlreadyLoaded =
                    modelAlreadyLoaded,
                requestedContextSize =
                    requestedContextSize
            )
        }

        val createResponse =
            gateway.createContext(
                contextSize = requestedContextSize
            )

        if (createResponse != CONTEXT_CREATED_RESPONSE) {
            rollbackNewModelWhenPossible(
                modelWasAlreadyLoaded =
                    modelAlreadyLoaded
            )

            return failure(
                stage =
                    LocalModelSessionFailureStage
                        .CONTEXT_CREATION,
                code =
                    LocalModelSessionFailureCode
                        .CONTEXT_CREATE_FAILED,
                details = createResponse
            )
        }

        if (!gateway.isContextReady()) {
            rollbackNewModelWhenPossible(
                modelWasAlreadyLoaded =
                    modelAlreadyLoaded
            )

            return failure(
                stage =
                    LocalModelSessionFailureStage
                        .CONTEXT_CREATION,
                code =
                    LocalModelSessionFailureCode
                        .CONTEXT_CREATE_FAILED,
                details =
                    "Il runtime non conferma la creazione del context."
            )
        }

        val actualContextSize =
            gateway.contextSize()

        if (
            actualContextSize !=
            requestedContextSize.toLong()
        ) {
            rollbackNewSessionWhenPossible(
                modelWasAlreadyLoaded =
                    modelAlreadyLoaded
            )

            return contextSizeMismatch(
                requestedContextSize =
                    requestedContextSize,
                actualContextSize =
                    actualContextSize
            )
        }

        return LocalModelSessionResult.Ready(
            modelAlreadyLoaded =
                modelAlreadyLoaded,
            contextAlreadyReady =
                false,
            contextSize =
                actualContextSize.toInt()
        )
    }

    private fun readyWithExistingContext(
        modelAlreadyLoaded: Boolean,
        requestedContextSize: Int
    ): LocalModelSessionResult {
        val actualContextSize =
            gateway.contextSize()

        if (
            actualContextSize !=
            requestedContextSize.toLong()
        ) {
            return contextSizeMismatch(
                requestedContextSize =
                    requestedContextSize,
                actualContextSize =
                    actualContextSize
            )
        }

        return LocalModelSessionResult.Ready(
            modelAlreadyLoaded =
                modelAlreadyLoaded,
            contextAlreadyReady =
                true,
            contextSize =
                actualContextSize.toInt()
        )
    }

    private fun releaseSafely():
            LocalModelSessionResult {
        if (gateway.isContextReady()) {
            val freeResponse =
                gateway.freeContext()

            if (freeResponse != CONTEXT_FREED_RESPONSE) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .CONTEXT_RELEASE,
                    code =
                        LocalModelSessionFailureCode
                            .CONTEXT_RELEASE_FAILED,
                    details = freeResponse
                )
            }

            if (gateway.isContextReady()) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .CONTEXT_RELEASE,
                    code =
                        LocalModelSessionFailureCode
                            .CONTEXT_RELEASE_FAILED,
                    details =
                        "Il context risulta ancora attivo."
                )
            }
        }

        if (gateway.isModelLoaded()) {
            val unloadResponse =
                gateway.unloadModel()

            if (unloadResponse != MODEL_UNLOADED_RESPONSE) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .MODEL_RELEASE,
                    code =
                        LocalModelSessionFailureCode
                            .MODEL_RELEASE_FAILED,
                    details = unloadResponse
                )
            }

            if (gateway.isModelLoaded()) {
                return failure(
                    stage =
                        LocalModelSessionFailureStage
                            .MODEL_RELEASE,
                    code =
                        LocalModelSessionFailureCode
                            .MODEL_RELEASE_FAILED,
                    details =
                        "Il modello risulta ancora caricato."
                )
            }
        }

        return LocalModelSessionResult.Released
    }

    private fun contextSizeMismatch(
        requestedContextSize: Int,
        actualContextSize: Long
    ): LocalModelSessionResult.Failure {
        return failure(
            stage =
                LocalModelSessionFailureStage
                    .CONTEXT_CREATION,
            code =
                LocalModelSessionFailureCode
                    .CONTEXT_SIZE_MISMATCH,
            details =
                "Context richiesto: $requestedContextSize; " +
                        "context effettivo: $actualContextSize."
        )
    }

    private fun rollbackNewModelWhenPossible(
        modelWasAlreadyLoaded: Boolean
    ) {
        if (
            !modelWasAlreadyLoaded &&
            gateway.isModelLoaded() &&
            !gateway.isContextReady()
        ) {
            gateway.unloadModel()
        }
    }

    private fun rollbackNewSessionWhenPossible(
        modelWasAlreadyLoaded: Boolean
    ) {
        if (gateway.isContextReady()) {
            gateway.freeContext()
        }

        rollbackNewModelWhenPossible(
            modelWasAlreadyLoaded =
                modelWasAlreadyLoaded
        )
    }

    private fun failure(
        stage: LocalModelSessionFailureStage,
        code: LocalModelSessionFailureCode,
        details: String?
    ): LocalModelSessionResult.Failure {
        return LocalModelSessionResult.Failure(
            stage = stage,
            code = code,
            details = details
        )
    }

    private companion object {
        const val MODEL_LOADED_RESPONSE =
            "OK|MODEL_LOADED"

        const val CONTEXT_CREATED_RESPONSE =
            "OK|CONTEXT_CREATED"

        const val CONTEXT_FREED_RESPONSE =
            "OK|CONTEXT_FREED"

        const val MODEL_UNLOADED_RESPONSE =
            "OK|MODEL_UNLOADED"
    }
}