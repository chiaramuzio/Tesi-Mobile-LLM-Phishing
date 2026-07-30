package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactModelOutputParseRequest
import com.example.phishingawareness.domain.model.CompactModelOutputParseResult
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationFailureStage
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.modeloutput.CompactModelOutputParser
import com.example.phishingawareness.domain.modeloutput.CompactParsedEmailMapper
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor

class GenerateCompactLocalEmailUseCase(
    private val buildCompactRuntimePromptUseCase:
    BuildCompactRuntimePromptUseCase,
    private val localModelExecutor:
    LocalModelExecutor,
    private val compactModelOutputParser:
    CompactModelOutputParser,
    private val compactParsedEmailMapper:
    CompactParsedEmailMapper
) : CompactLocalEmailGenerator {

    override operator fun invoke(
        request: GenerationRequest,
        options: LocalEmailGenerationOptions
    ): LocalEmailGenerationResult {
        val expectedScenario =
            mapScenario(request.scenarioId)
                ?: return LocalEmailGenerationResult.Failure(
                    stage =
                        LocalEmailGenerationFailureStage
                            .REQUEST_MAPPING,
                    details =
                        "Scenario non valido: ${request.scenarioId}"
                )

        val promptResult =
            buildCompactRuntimePromptUseCase(request)

        val artifact =
            when (promptResult) {
                is RuntimePromptGenerationResult.Success ->
                    promptResult.artifact

                is RuntimePromptGenerationResult.Failure ->
                    return LocalEmailGenerationResult.Failure(
                        stage =
                            LocalEmailGenerationFailureStage
                                .PROMPT_BUILDING,
                        details =
                            "${promptResult.stage}: " +
                                    promptResult.details
                    )
            }

        val executionResult =
            localModelExecutor.execute(
                request = LocalModelExecutionRequest(
                    prompt = artifact.text,
                    promptSha256 =
                        artifact.metadata.promptSha256,
                    seed = options.seed,
                    contextSize = options.contextSize,
                    maxGeneratedTokens =
                        options.maxGeneratedTokens,
                    temperature = options.temperature,
                    topK = options.topK,
                    topP = options.topP,
                    minP = options.minP,
                    repeatPenalty =
                        options.repeatPenalty
                )
            )

        val executionSuccess =
            when (executionResult) {
                is LocalModelExecutionResult.Success ->
                    executionResult

                is LocalModelExecutionResult.Failure ->
                    return LocalEmailGenerationResult.Failure(
                        stage =
                            LocalEmailGenerationFailureStage
                                .MODEL_EXECUTION,
                        details =
                            buildString {
                                append(executionResult.code.name)

                                executionResult.details
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { details ->
                                        append(": ")
                                        append(details)
                                    }
                            }
                    )
            }

        val parseResult =
            compactModelOutputParser.parse(
                request =
                    CompactModelOutputParseRequest(
                        rawOutput =
                            executionSuccess.rawOutput,
                        expectedScenario =
                            expectedScenario
                    )
            )

        val parsedEmail =
            when (parseResult) {
                is CompactModelOutputParseResult.Success ->
                    parseResult.email

                is CompactModelOutputParseResult.Failure ->
                    return LocalEmailGenerationResult.Failure(
                        stage =
                            LocalEmailGenerationFailureStage
                                .OUTPUT_PARSING,
                        details =
                            parseResult.issues
                                .joinToString(
                                    separator = " | "
                                ) { issue ->
                                    buildString {
                                        append(issue.code.name)

                                        issue.field
                                            ?.let { field ->
                                                append("[")
                                                append(field)
                                                append("]")
                                            }

                                        issue.details
                                            ?.takeIf {
                                                it.isNotBlank()
                                            }
                                            ?.let { details ->
                                                append(": ")
                                                append(details)
                                            }
                                    }
                                }
                    )
            }

        val generatedEmail =
            try {
                compactParsedEmailMapper.map(
                    parsedEmail
                )
            } catch (
                exception: IllegalArgumentException
            ) {
                return LocalEmailGenerationResult.Failure(
                    stage =
                        LocalEmailGenerationFailureStage
                            .EMAIL_MAPPING,
                    details =
                        exception.message
                            ?: "Errore durante il mapping"
                )
            }

        return LocalEmailGenerationResult.Success(
            email = generatedEmail,
            promptMetadata = artifact.metadata,
            executionMetadata =
                executionSuccess.metadata
        )
    }

    private fun mapScenario(
        scenarioId: String
    ): Scenario? {
        val normalizedScenarioId =
            scenarioId.trim().uppercase()

        return enumValues<Scenario>()
            .firstOrNull { scenario ->
                scenario.name == normalizedScenarioId
            }
    }
}