package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.PromptParameterResolutionRequest
import com.example.phishingawareness.domain.model.PromptParameterResolutionResult
import com.example.phishingawareness.domain.model.RuntimePromptGenerationFailureStage
import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.RuntimePromptSectionResolutionResult
import com.example.phishingawareness.domain.prompt.PromptBuilder
import com.example.phishingawareness.domain.prompt.PromptParameterResolver
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.prompt.RuntimePromptSectionResolver

class DeterministicRuntimePromptGenerationOrchestrator(
    private val parameterResolver: PromptParameterResolver,
    private val sectionResolver: RuntimePromptSectionResolver,
    private val promptBuilder: PromptBuilder
) : RuntimePromptGenerationOrchestrator {

    override fun generate(
        request: RuntimePromptGenerationRequest
    ): RuntimePromptGenerationResult {
        val parameterResult =
            parameterResolver.resolve(
                PromptParameterResolutionRequest(
                    configurationId = request.configurationId,
                    userConfiguration = request.userConfiguration,
                    language = request.language
                )
            )

        val parameters =
            when (parameterResult) {
                is PromptParameterResolutionResult.Success ->
                    parameterResult.parameters

                is PromptParameterResolutionResult.Failure ->
                    return RuntimePromptGenerationResult.Failure(
                        stage =
                            RuntimePromptGenerationFailureStage
                                .PARAMETER_RESOLUTION,
                        details =
                            parameterResult.issues.toDetails()
                    )
            }

        val sectionResult =
            sectionResolver.resolve(parameters)

        val configuration =
            when (sectionResult) {
                is RuntimePromptSectionResolutionResult.Success ->
                    sectionResult.configuration

                is RuntimePromptSectionResolutionResult.Failure ->
                    return RuntimePromptGenerationResult.Failure(
                        stage =
                            RuntimePromptGenerationFailureStage
                                .SECTION_RESOLUTION,
                        details =
                            sectionResult.issues.toDetails()
                    )
            }

        return when (
            val buildResult =
                promptBuilder.build(
                    configuration = configuration,
                    context = request.buildContext
                )
        ) {
            is PromptBuildResult.Success ->
                RuntimePromptGenerationResult.Success(
                    artifact = buildResult.artifact
                )

            is PromptBuildResult.Failure ->
                RuntimePromptGenerationResult.Failure(
                    stage =
                        RuntimePromptGenerationFailureStage
                            .PROMPT_BUILDING,
                    details = buildResult.issues.toDetails()
                )
        }
    }

    private fun List<*>.toDetails(): String {
        return joinToString(
            separator = " | "
        ) { issue ->
            issue.toString()
        }
    }
}