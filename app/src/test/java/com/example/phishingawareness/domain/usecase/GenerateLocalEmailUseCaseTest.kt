package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LocalEmailGenerationFailureStage
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.LocalModelExecutionFailureCode
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import com.example.phishingawareness.domain.model.ModelOutputParseIssue
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.ModelOutputParseRequest
import com.example.phishingawareness.domain.model.ModelOutputParseResult
import com.example.phishingawareness.domain.model.ParsedPhishingEmail
import com.example.phishingawareness.domain.model.ParsedPhishingIndicator
import com.example.phishingawareness.domain.model.PromptArtifact
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.modeloutput.ModelOutputParser
import com.example.phishingawareness.domain.modeloutput.ParsedEmailMapper
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateLocalEmailUseCaseTest {

    @Test
    fun invoke_successfulPipeline_returnsGeneratedEmailAndMetadata() {
        val executor =
            CapturingLocalModelExecutor()

        val useCase =
            useCase(
                executor = executor
            )

        val result =
            useCase(
                request = validRequest(),
                options =
                    LocalEmailGenerationOptions(
                        seed = 404
                    )
            )

        assertTrue(
            result is LocalEmailGenerationResult.Success
        )

        val success =
            result as LocalEmailGenerationResult.Success

        assertEquals(
            "Servizio Sicurezza",
            success.email.senderName
        )

        assertEquals(
            setOf("URGENCY_PRESSURE"),
            success.email.presentIndicatorIds
        )

        assertEquals(
            "hash-test",
            success.promptMetadata.promptSha256
        )

        assertEquals(
            404,
            success.executionMetadata.seed
        )

        assertEquals(
            "Prompt runtime",
            executor.receivedRequest?.prompt
        )

        assertEquals(
            8192,
            executor.receivedRequest?.contextSize
        )

        assertEquals(
            1200,
            executor.receivedRequest
                ?.maxGeneratedTokens
        )
    }

    @Test
    fun invoke_invalidScenario_returnsRequestMappingFailure() {
        val result =
            useCase()(
                request =
                    validRequest().copy(
                        scenarioId = "UNKNOWN"
                    )
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage
                .REQUEST_MAPPING,
            failure.stage
        )
    }

    @Test
    fun invoke_promptFailure_returnsPromptBuildingFailure() {
        val buildUseCase =
            StubBuildRuntimePromptUseCase(
                result =
                    RuntimePromptGenerationResult.Failure(
                        stage =
                            com.example.phishingawareness
                                .domain.model
                                .RuntimePromptGenerationFailureStage
                                .PARAMETER_RESOLUTION,
                        details = "profilo assente"
                    )
            )

        val result =
            useCase(
                buildUseCase = buildUseCase
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage
                .PROMPT_BUILDING,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "profilo assente"
            )
        )
    }

    @Test
    fun invoke_executionFailure_returnsModelExecutionFailure() {
        val executor =
            CapturingLocalModelExecutor(
                result =
                    LocalModelExecutionResult.Failure(
                        code =
                            LocalModelExecutionFailureCode
                                .MODEL_NOT_AVAILABLE,
                        details = "modello assente"
                    )
            )

        val result =
            useCase(
                executor = executor
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage
                .MODEL_EXECUTION,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "MODEL_NOT_AVAILABLE"
            )
        )
    }

    @Test
    fun invoke_parseFailure_returnsOutputParsingFailure() {
        val parser =
            StubModelOutputParser(
                result =
                    ModelOutputParseResult.Failure(
                        issues = listOf(
                            ModelOutputParseIssue(
                                code =
                                    ModelOutputParseIssueCode
                                        .MALFORMED_JSON,
                                field = "rawOutput"
                            )
                        )
                    )
            )

        val result =
            useCase(
                parser = parser
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage
                .OUTPUT_PARSING,
            failure.stage
        )

        assertTrue(
            failure.details.contains(
                "MALFORMED_JSON"
            )
        )
    }

    @Test
    fun invoke_mapperFailure_returnsEmailMappingFailure() {
        val mapper =
            object : ParsedEmailMapper {
                override fun map(
                    email: ParsedPhishingEmail
                ): GeneratedEmail {
                    throw IllegalArgumentException(
                        "mapping non valido"
                    )
                }
            }

        val result =
            useCase(
                mapper = mapper
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage
                .EMAIL_MAPPING,
            failure.stage
        )

        assertEquals(
            "mapping non valido",
            failure.details
        )
    }

    private fun useCase(
        buildUseCase: BuildRuntimePromptUseCase =
            StubBuildRuntimePromptUseCase(
                result =
                    RuntimePromptGenerationResult.Success(
                        artifact = promptArtifact()
                    )
            ),
        executor: LocalModelExecutor =
            CapturingLocalModelExecutor(),
        parser: ModelOutputParser =
            StubModelOutputParser(
                result =
                    ModelOutputParseResult.Success(
                        email = parsedEmail()
                    )
            ),
        mapper: ParsedEmailMapper =
            StubParsedEmailMapper()
    ): GenerateLocalEmailUseCase {
        return GenerateLocalEmailUseCase(
            buildRuntimePromptUseCase =
                buildUseCase,
            localModelExecutor = executor,
            modelOutputParser = parser,
            parsedEmailMapper = mapper
        )
    }

    private fun validRequest(): GenerationRequest {
        return GenerationRequest(
            scenarioId = "BANKING",
            difficulty = "MEDIUM",
            length = "MEDIUM"
        )
    }

    private fun promptArtifact(): PromptArtifact {
        return PromptArtifact(
            text = "Prompt runtime",
            metadata =
                PromptMetadata(
                    buildContext =
                        PromptBuildContext(
                            builderVersion = "1",
                            templateId =
                                "RUNTIME_MODULAR",
                            templateVersion = "1",
                            libraryId =
                                "phishing-awareness-library",
                            libraryVersion = "0.3.0",
                            librarySchemaVersion = 2
                        ),
                    resolvedConfigurationId =
                        "BANKING_MEDIUM_MEDIUM",
                    promptSha256 = "hash-test"
                )
        )
    }

    private fun parsedEmail(): ParsedPhishingEmail {
        return ParsedPhishingEmail(
            scenario = "Bancario e pagamenti",
            difficulty = "Media",
            length = "Media",
            senderName = "Servizio Sicurezza",
            senderAddress =
                "sicurezza@bancaesempio.invalid",
            recipient =
                "utente@bancaesempio.invalid",
            subject = "Accesso sospetto",
            body = "Corpo della simulazione",
            pretext = "Accesso sospetto",
            ctaType = "LOGIN",
            ctaText = "Accedi al conto",
            presentIndicators = listOf(
                ParsedPhishingIndicator(
                    promptId = "IND_URGENCY",
                    internalId = "URGENCY_PRESSURE",
                    evidence = "Evidence",
                    explanation = "Spiegazione"
                )
            ),
            credibilityElements =
                listOf("tono professionale"),
            educationalSummary =
                "Riepilogo educativo"
        )
    }

    private class StubBuildRuntimePromptUseCase(
        private val result:
        RuntimePromptGenerationResult
    ) : BuildRuntimePromptUseCase(
        orchestrator =
            object :
                com.example.phishingawareness
                .domain.prompt
                .RuntimePromptGenerationOrchestrator {

                override fun generate(
                    request:
                    com.example.phishingawareness
                    .domain.model
                    .RuntimePromptGenerationRequest
                ): RuntimePromptGenerationResult {
                    return result
                }
            },
        libraryRepository =
            object :
                com.example.phishingawareness
                .domain.repository
                .LibraryRepository {

                override fun getManifest() =
                    error("Non usato")

                override fun getScenarios() =
                    emptyList<com.example.phishingawareness
                    .domain.model.ScenarioDefinition>()

                override fun getEnabledScenarios() =
                    emptyList<com.example.phishingawareness
                    .domain.model.ScenarioDefinition>()

                override fun getIndicators() =
                    emptyList<com.example.phishingawareness
                    .domain.model.IndicatorDefinition>()

                override fun getDistractors() =
                    emptyList<com.example.phishingawareness
                    .domain.model.DistractorDefinition>()

                override fun getIndicatorsForScenario(
                    scenarioId: String
                ) =
                    emptyList<com.example.phishingawareness
                    .domain.model.IndicatorDefinition>()

                override fun getDistractorsForScenario(
                    scenarioId: String
                ) =
                    emptyList<com.example.phishingawareness
                    .domain.model.DistractorDefinition>()

                override fun getIndicatorById(
                    indicatorId: String
                ) =
                    null

                override fun getIndicatorByPromptId(
                    promptId: String
                ) =
                    null
            }
    ) {
        override operator fun invoke(
            request: GenerationRequest
        ): RuntimePromptGenerationResult {
            return result
        }
    }

    private class CapturingLocalModelExecutor(
        private val result:
        LocalModelExecutionResult? = null
    ) : LocalModelExecutor {

        var receivedRequest:
                LocalModelExecutionRequest? = null
            private set

        override fun execute(
            request: LocalModelExecutionRequest
        ): LocalModelExecutionResult {
            receivedRequest = request

            return result
                ?: LocalModelExecutionResult.Success(
                    rawOutput = """{"result":"ok"}""",
                    metadata =
                        LocalModelExecutionMetadata(
                            promptSha256 =
                                request.promptSha256,
                            seed = request.seed,
                            generatedCharacterCount = 15
                        )
                )
        }
    }

    private class StubModelOutputParser(
        private val result:
        ModelOutputParseResult
    ) : ModelOutputParser {

        override fun parse(
            request: ModelOutputParseRequest
        ): ModelOutputParseResult {
            return result
        }
    }

    private class StubParsedEmailMapper :
        ParsedEmailMapper {

        override fun map(
            email: ParsedPhishingEmail
        ): GeneratedEmail {
            return GeneratedEmail(
                senderName = email.senderName,
                senderAddress = email.senderAddress,
                subject = email.subject,
                body = email.body,
                presentIndicatorIds =
                    email.presentIndicators
                        .map { indicator ->
                            indicator.internalId
                        }
                        .toSet()
            )
        }
    }
}