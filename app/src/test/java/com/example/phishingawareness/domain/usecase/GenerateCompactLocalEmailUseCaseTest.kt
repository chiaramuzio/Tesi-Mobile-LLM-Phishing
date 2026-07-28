package com.example.phishingawareness.domain.usecase

import com.example.phishingawareness.domain.model.CompactModelOutputParseRequest
import com.example.phishingawareness.domain.model.CompactModelOutputParseResult
import com.example.phishingawareness.domain.model.CompactParsedPhishingEmail
import com.example.phishingawareness.domain.model.CompactParsedPhishingIndicator
import com.example.phishingawareness.domain.model.GeneratedEmail
import com.example.phishingawareness.domain.model.GenerationRequest
import com.example.phishingawareness.domain.model.LibraryManifest
import com.example.phishingawareness.domain.model.LocalEmailGenerationFailureStage
import com.example.phishingawareness.domain.model.LocalEmailGenerationOptions
import com.example.phishingawareness.domain.model.LocalEmailGenerationResult
import com.example.phishingawareness.domain.model.LocalModelExecutionFailureCode
import com.example.phishingawareness.domain.model.LocalModelExecutionMetadata
import com.example.phishingawareness.domain.model.LocalModelExecutionRequest
import com.example.phishingawareness.domain.model.LocalModelExecutionResult
import com.example.phishingawareness.domain.model.ModelOutputParseIssue
import com.example.phishingawareness.domain.model.ModelOutputParseIssueCode
import com.example.phishingawareness.domain.model.PromptArtifact
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.RuntimePromptGenerationFailureStage
import com.example.phishingawareness.domain.model.RuntimePromptGenerationRequest
import com.example.phishingawareness.domain.model.RuntimePromptGenerationResult
import com.example.phishingawareness.domain.model.ScenarioDefinition
import com.example.phishingawareness.domain.model.IndicatorDefinition
import com.example.phishingawareness.domain.model.DistractorDefinition
import com.example.phishingawareness.domain.modeloutput.CompactModelOutputParser
import com.example.phishingawareness.domain.modeloutput.CompactParsedEmailMapper
import com.example.phishingawareness.domain.modelruntime.LocalModelExecutor
import com.example.phishingawareness.domain.prompt.RuntimePromptGenerationOrchestrator
import com.example.phishingawareness.domain.repository.LibraryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateCompactLocalEmailUseCaseTest {

    @Test
    fun invoke_successfulPipeline_returnsEmailAndMetadata() {
        val executor =
            CapturingLocalModelExecutor()

        val result =
            useCase(
                executor = executor
            )(
                request = validRequest(),
                options =
                    LocalEmailGenerationOptions(
                        seed = 404,
                        contextSize = 8192,
                        maxGeneratedTokens = 600,
                        temperature = 0.4f,
                        topK = 40,
                        topP = 0.90f,
                        minP = 0.05f,
                        repeatPenalty = 1.05f
                    )
            )

        val success =
            result as LocalEmailGenerationResult.Success

        assertEquals(
            "Supporto IT",
            success.email.senderName
        )

        assertEquals(
            setOf("URGENCY_PRESSURE"),
            success.email.presentIndicatorIds
        )

        assertEquals(
            "compact-hash",
            success.promptMetadata.promptSha256
        )

        assertEquals(
            404,
            success.executionMetadata.seed
        )

        val executionRequest =
            requireNotNull(
                executor.receivedRequest
            )

        assertEquals(
            "Prompt compatto",
            executionRequest.prompt
        )
        assertEquals(404, executionRequest.seed)
        assertEquals(8192, executionRequest.contextSize)
        assertEquals(600, executionRequest.maxGeneratedTokens)
        assertEquals(0.4f, executionRequest.temperature)
        assertEquals(40, executionRequest.topK)
        assertEquals(0.90f, executionRequest.topP)
        assertEquals(0.05f, executionRequest.minP)
        assertEquals(1.05f, executionRequest.repeatPenalty)
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
            LocalEmailGenerationFailureStage.REQUEST_MAPPING,
            failure.stage
        )
    }

    @Test
    fun invoke_promptFailure_returnsPromptBuildingFailure() {
        val result =
            useCase(
                buildResult =
                    RuntimePromptGenerationResult.Failure(
                        stage =
                            RuntimePromptGenerationFailureStage
                                .PARAMETER_RESOLUTION,
                        details = "profilo assente"
                    )
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage.PROMPT_BUILDING,
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
        val result =
            useCase(
                executor =
                    CapturingLocalModelExecutor(
                        result =
                            LocalModelExecutionResult.Failure(
                                code =
                                    LocalModelExecutionFailureCode
                                        .MODEL_NOT_AVAILABLE,
                                details = "modello assente"
                            )
                    )
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage.MODEL_EXECUTION,
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
        val result =
            useCase(
                parser =
                    StubCompactModelOutputParser(
                        result =
                            CompactModelOutputParseResult.Failure(
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
            )(
                request = validRequest()
            )

        val failure =
            result as LocalEmailGenerationResult.Failure

        assertEquals(
            LocalEmailGenerationFailureStage.OUTPUT_PARSING,
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
            object : CompactParsedEmailMapper {
                override fun map(
                    email: CompactParsedPhishingEmail
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
            LocalEmailGenerationFailureStage.EMAIL_MAPPING,
            failure.stage
        )

        assertEquals(
            "mapping non valido",
            failure.details
        )
    }

    private fun useCase(
        buildResult: RuntimePromptGenerationResult =
            RuntimePromptGenerationResult.Success(
                artifact = promptArtifact()
            ),
        executor: LocalModelExecutor =
            CapturingLocalModelExecutor(),
        parser: CompactModelOutputParser =
            StubCompactModelOutputParser(
                result =
                    CompactModelOutputParseResult.Success(
                        email = parsedEmail()
                    )
            ),
        mapper: CompactParsedEmailMapper =
            StubCompactParsedEmailMapper()
    ): GenerateCompactLocalEmailUseCase {
        return GenerateCompactLocalEmailUseCase(
            buildCompactRuntimePromptUseCase =
                BuildCompactRuntimePromptUseCase(
                    orchestrator =
                        StubPromptOrchestrator(
                            result = buildResult
                        ),
                    libraryRepository =
                        StubLibraryRepository()
                ),
            localModelExecutor = executor,
            compactModelOutputParser = parser,
            compactParsedEmailMapper = mapper
        )
    }

    private fun validRequest(): GenerationRequest {
        return GenerationRequest(
            scenarioId = "ACCOUNT_IT",
            difficulty = "MEDIUM",
            length = "MEDIUM"
        )
    }

    private fun promptArtifact(): PromptArtifact {
        return PromptArtifact(
            text = "Prompt compatto",
            metadata =
                PromptMetadata(
                    buildContext =
                        PromptBuildContext(
                            builderVersion = "1",
                            templateId =
                                "RUNTIME_MODULAR_COMPACT",
                            templateVersion = "1",
                            libraryId =
                                "phishing-awareness-library",
                            libraryVersion = "0.3.0",
                            librarySchemaVersion = 2
                        ),
                    resolvedConfigurationId =
                        "ACCOUNT_IT_MEDIUM_MEDIUM",
                    promptSha256 = "compact-hash"
                )
        )
    }

    private fun parsedEmail():
            CompactParsedPhishingEmail {
        return CompactParsedPhishingEmail(
            senderName = "Supporto IT",
            senderAddress =
                "supporto@aziendaesempio.invalid",
            subject = "Aggiornamento password",
            body = "Corpo della simulazione",
            presentIndicators = listOf(
                CompactParsedPhishingIndicator(
                    promptId = "IND_URGENCY",
                    internalId = "URGENCY_PRESSURE",
                    evidence = "Evidence"
                )
            )
        )
    }

    private class StubPromptOrchestrator(
        private val result:
        RuntimePromptGenerationResult
    ) : RuntimePromptGenerationOrchestrator {

        override fun generate(
            request: RuntimePromptGenerationRequest
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

    private class StubCompactModelOutputParser(
        private val result:
        CompactModelOutputParseResult
    ) : CompactModelOutputParser {

        override fun parse(
            request: CompactModelOutputParseRequest
        ): CompactModelOutputParseResult {
            return result
        }
    }

    private class StubCompactParsedEmailMapper :
        CompactParsedEmailMapper {

        override fun map(
            email: CompactParsedPhishingEmail
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

    private class StubLibraryRepository :
        LibraryRepository {

        override fun getManifest(): LibraryManifest {
            return LibraryManifest(
                libraryId =
                    "phishing-awareness-library",
                version = "0.3.0",
                schemaVersion = 2,
                language = "it",
                activeScenarios =
                    listOf(
                        "BANKING",
                        "ACCOUNT_IT"
                    ),
                resources =
                    emptyList()
            )
        }

        override fun getScenarios():
                List<ScenarioDefinition> =
            emptyList()

        override fun getEnabledScenarios():
                List<ScenarioDefinition> =
            emptyList()

        override fun getIndicators():
                List<IndicatorDefinition> =
            emptyList()

        override fun getDistractors():
                List<DistractorDefinition> =
            emptyList()

        override fun getIndicatorsForScenario(
            scenarioId: String
        ): List<IndicatorDefinition> =
            emptyList()

        override fun getDistractorsForScenario(
            scenarioId: String
        ): List<DistractorDefinition> =
            emptyList()

        override fun getIndicatorById(
            indicatorId: String
        ): IndicatorDefinition? =
            null

        override fun getIndicatorByPromptId(
            promptId: String
        ): IndicatorDefinition? =
            null
    }
}