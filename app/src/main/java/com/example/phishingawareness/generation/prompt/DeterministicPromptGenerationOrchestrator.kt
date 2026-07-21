package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.PromptGenerationFailureStage
import com.example.phishingawareness.domain.model.PromptGenerationRequest
import com.example.phishingawareness.domain.model.PromptGenerationResult
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult
import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult
import com.example.phishingawareness.domain.prompt.PromptBuilder
import com.example.phishingawareness.domain.prompt.PromptGenerationOrchestrator
import com.example.phishingawareness.domain.prompt.PromptTemplateLoader
import com.example.phishingawareness.domain.prompt.PromptTemplateResolver

/**
 * Coordina deterministicamente l'intera pipeline di generazione del prompt.
 *
 * Ogni fase viene delegata al relativo componente:
 * caricamento del template, risoluzione della configurazione
 * e costruzione dell'artefatto finale.
 */
class DeterministicPromptGenerationOrchestrator(
    private val templateLoader: PromptTemplateLoader,
    private val templateResolver: PromptTemplateResolver,
    private val promptBuilder: PromptBuilder
) : PromptGenerationOrchestrator {

    override fun generate(
        request: PromptGenerationRequest
    ): PromptGenerationResult {
        val template = when (
            val loadResult = templateLoader.load(request.templateId)
        ) {
            is PromptTemplateLoadResult.Success -> {
                loadResult.template
            }

            is PromptTemplateLoadResult.Failure -> {
                return PromptGenerationResult.Failure(
                    stage = PromptGenerationFailureStage.TEMPLATE_LOADING,
                    details = buildLoadFailureDetails(loadResult)
                )
            }
        }

        val configuration = when (
            val resolutionResult = templateResolver.resolve(
                PromptTemplateResolutionRequest(
                    configurationId = request.configurationId,
                    template = template,
                    difficulty = request.difficulty,
                    length = request.length,
                    language = request.language
                )
            )
        ) {
            is PromptTemplateResolutionResult.Success -> {
                resolutionResult.configuration
            }

            is PromptTemplateResolutionResult.Failure -> {
                return PromptGenerationResult.Failure(
                    stage = PromptGenerationFailureStage.TEMPLATE_RESOLUTION,
                    details = resolutionResult.issues.joinToString(
                        separator = " | "
                    ) { issue ->
                        listOfNotNull(
                            issue.code.name,
                            issue.field,
                            issue.details
                        ).joinToString(separator = ": ")
                    }
                )
            }
        }

        return when (
            val buildResult = promptBuilder.build(
                configuration = configuration,
                context = request.buildContext
            )
        ) {
            is PromptBuildResult.Success -> {
                PromptGenerationResult.Success(
                    artifact = buildResult.artifact
                )
            }

            is PromptBuildResult.Failure -> {
                PromptGenerationResult.Failure(
                    stage = PromptGenerationFailureStage.PROMPT_BUILDING,
                    details = buildResult.issues.joinToString(
                        separator = " | "
                    ) { issue ->
                        listOfNotNull(
                            issue.code.name,
                            issue.field,
                            issue.details
                        ).joinToString(separator = ": ")
                    }
                )
            }
        }
    }

    private fun buildLoadFailureDetails(
        failure: PromptTemplateLoadResult.Failure
    ): String {
        return listOfNotNull(
            failure.code.name,
            failure.templateId.name,
            failure.details
        ).joinToString(separator = ": ")
    }
}