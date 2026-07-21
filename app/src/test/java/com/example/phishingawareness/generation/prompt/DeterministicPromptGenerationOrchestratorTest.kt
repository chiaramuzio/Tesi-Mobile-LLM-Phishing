package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.Difficulty
import com.example.phishingawareness.domain.model.ExerciseLength
import com.example.phishingawareness.domain.model.PromptArtifact
import com.example.phishingawareness.domain.model.PromptBuildContext
import com.example.phishingawareness.domain.model.PromptBuildIssue
import com.example.phishingawareness.domain.model.PromptBuildIssueCode
import com.example.phishingawareness.domain.model.PromptBuildResult
import com.example.phishingawareness.domain.model.PromptGenerationFailureStage
import com.example.phishingawareness.domain.model.PromptGenerationRequest
import com.example.phishingawareness.domain.model.PromptGenerationResult
import com.example.phishingawareness.domain.model.PromptMetadata
import com.example.phishingawareness.domain.model.PromptTemplate
import com.example.phishingawareness.domain.model.PromptTemplateId
import com.example.phishingawareness.domain.model.PromptTemplateLoadIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateLoadResult
import com.example.phishingawareness.domain.model.PromptTemplateResolutionIssue
import com.example.phishingawareness.domain.model.PromptTemplateResolutionIssueCode
import com.example.phishingawareness.domain.model.PromptTemplateResolutionRequest
import com.example.phishingawareness.domain.model.PromptTemplateResolutionResult
import com.example.phishingawareness.domain.model.PromptTemplateSection
import com.example.phishingawareness.domain.model.ResolvedGenerationConfig
import com.example.phishingawareness.domain.model.ResolvedPromptSection
import com.example.phishingawareness.domain.model.Scenario
import com.example.phishingawareness.domain.prompt.PromptBuilder
import com.example.phishingawareness.domain.prompt.PromptTemplateLoader
import com.example.phishingawareness.domain.prompt.PromptTemplateResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicPromptGenerationOrchestratorTest {

    @Test
    fun generate_whenAllStagesSucceed_returnsArtifact() {
        val expectedArtifact = artifact()

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = successfulLoader(),
            templateResolver = successfulResolver(),
            promptBuilder = successfulBuilder(expectedArtifact)
        )

        val result = orchestrator.generate(validRequest())

        assertEquals(
            PromptGenerationResult.Success(expectedArtifact),
            result
        )
    }

    @Test
    fun generate_whenLoadingFails_returnsLoadingFailure() {
        val loader = object : PromptTemplateLoader {
            override fun load(
                templateId: PromptTemplateId
            ): PromptTemplateLoadResult {
                return PromptTemplateLoadResult.Failure(
                    code = PromptTemplateLoadIssueCode.TEXT_READ_FAILURE,
                    templateId = templateId,
                    details = "ASSET_NOT_FOUND"
                )
            }
        }

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = loader,
            templateResolver = successfulResolver(),
            promptBuilder = successfulBuilder(artifact())
        )

        val result = orchestrator.generate(validRequest())

        assertTrue(result is PromptGenerationResult.Failure)

        val failure = result as PromptGenerationResult.Failure

        assertEquals(
            PromptGenerationFailureStage.TEMPLATE_LOADING,
            failure.stage
        )
        assertTrue(
            failure.details?.contains("TEXT_READ_FAILURE") == true
        )
        assertTrue(
            failure.details?.contains("ASSET_NOT_FOUND") == true
        )
    }

    @Test
    fun generate_whenResolutionFails_returnsResolutionFailure() {
        val resolver = object : PromptTemplateResolver {
            override fun resolve(
                request: PromptTemplateResolutionRequest
            ): PromptTemplateResolutionResult {
                return PromptTemplateResolutionResult.Failure(
                    issues = listOf(
                        PromptTemplateResolutionIssue(
                            code = PromptTemplateResolutionIssueCode
                                .MISSING_LANGUAGE,
                            field = "language"
                        )
                    )
                )
            }
        }

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = successfulLoader(),
            templateResolver = resolver,
            promptBuilder = successfulBuilder(artifact())
        )

        val result = orchestrator.generate(validRequest())

        assertTrue(result is PromptGenerationResult.Failure)

        val failure = result as PromptGenerationResult.Failure

        assertEquals(
            PromptGenerationFailureStage.TEMPLATE_RESOLUTION,
            failure.stage
        )
        assertTrue(
            failure.details?.contains("MISSING_LANGUAGE") == true
        )
        assertTrue(
            failure.details?.contains("language") == true
        )
    }

    @Test
    fun generate_whenBuildingFails_returnsBuildingFailure() {
        val builder = object : PromptBuilder {
            override fun build(
                configuration: ResolvedGenerationConfig,
                context: PromptBuildContext
            ): PromptBuildResult {
                return PromptBuildResult.Failure(
                    issues = listOf(
                        PromptBuildIssue(
                            code = PromptBuildIssueCode
                                .MISSING_REQUIRED_SECTION,
                            field = "sections"
                        )
                    )
                )
            }
        }

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = successfulLoader(),
            templateResolver = successfulResolver(),
            promptBuilder = builder
        )

        val result = orchestrator.generate(validRequest())

        assertTrue(result is PromptGenerationResult.Failure)

        val failure = result as PromptGenerationResult.Failure

        assertEquals(
            PromptGenerationFailureStage.PROMPT_BUILDING,
            failure.stage
        )
        assertTrue(
            failure.details?.contains(
                "MISSING_REQUIRED_SECTION"
            ) == true
        )
        assertTrue(
            failure.details?.contains("sections") == true
        )
    }

    @Test
    fun generate_passesRequestValuesToResolver() {
        var receivedRequest: PromptTemplateResolutionRequest? = null

        val resolver = object : PromptTemplateResolver {
            override fun resolve(
                request: PromptTemplateResolutionRequest
            ): PromptTemplateResolutionResult {
                receivedRequest = request

                return PromptTemplateResolutionResult.Success(
                    configuration = resolvedConfiguration()
                )
            }
        }

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = successfulLoader(),
            templateResolver = resolver,
            promptBuilder = successfulBuilder(artifact())
        )

        val request = validRequest()

        orchestrator.generate(request)

        assertEquals(
            request.configurationId,
            receivedRequest?.configurationId
        )
        assertEquals(
            request.difficulty,
            receivedRequest?.difficulty
        )
        assertEquals(
            request.length,
            receivedRequest?.length
        )
        assertEquals(
            request.language,
            receivedRequest?.language
        )
        assertEquals(
            request.templateId,
            receivedRequest?.template?.id
        )
    }

    @Test
    fun generate_passesResolvedConfigurationAndContextToBuilder() {
        var receivedConfiguration: ResolvedGenerationConfig? = null
        var receivedContext: PromptBuildContext? = null

        val builder = object : PromptBuilder {
            override fun build(
                configuration: ResolvedGenerationConfig,
                context: PromptBuildContext
            ): PromptBuildResult {
                receivedConfiguration = configuration
                receivedContext = context

                return PromptBuildResult.Success(
                    artifact = artifact()
                )
            }
        }

        val orchestrator = DeterministicPromptGenerationOrchestrator(
            templateLoader = successfulLoader(),
            templateResolver = successfulResolver(),
            promptBuilder = builder
        )

        val request = validRequest()

        orchestrator.generate(request)

        assertEquals(
            resolvedConfiguration(),
            receivedConfiguration
        )
        assertEquals(
            request.buildContext,
            receivedContext
        )
    }

    private fun successfulLoader(): PromptTemplateLoader {
        return object : PromptTemplateLoader {
            override fun load(
                templateId: PromptTemplateId
            ): PromptTemplateLoadResult {
                return PromptTemplateLoadResult.Success(
                    template = template()
                )
            }
        }
    }

    private fun successfulResolver(): PromptTemplateResolver {
        return object : PromptTemplateResolver {
            override fun resolve(
                request: PromptTemplateResolutionRequest
            ): PromptTemplateResolutionResult {
                return PromptTemplateResolutionResult.Success(
                    configuration = resolvedConfiguration()
                )
            }
        }
    }

    private fun successfulBuilder(
        expectedArtifact: PromptArtifact
    ): PromptBuilder {
        return object : PromptBuilder {
            override fun build(
                configuration: ResolvedGenerationConfig,
                context: PromptBuildContext
            ): PromptBuildResult {
                return PromptBuildResult.Success(
                    artifact = expectedArtifact
                )
            }
        }
    }

    private fun validRequest(): PromptGenerationRequest {
        return PromptGenerationRequest(
            configurationId =
                "BANKING_ZERO_SHOT_V12_MEDIUM_MEDIUM_IT",
            templateId =
                PromptTemplateId.BANKING_ZERO_SHOT_V12,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it",
            buildContext = buildContext()
        )
    }

    private fun template(): PromptTemplate {
        return PromptTemplate(
            id = PromptTemplateId.BANKING_ZERO_SHOT_V12,
            version = "12",
            scenario = Scenario.BANKING,
            sections = listOf(
                PromptTemplateSection(
                    id = "FULL_PROMPT",
                    content = "Prompt congelato"
                )
            )
        )
    }

    private fun resolvedConfiguration(): ResolvedGenerationConfig {
        return ResolvedGenerationConfig(
            configurationId =
                "BANKING_ZERO_SHOT_V12_MEDIUM_MEDIUM_IT",
            scenario = Scenario.BANKING,
            difficulty = Difficulty.MEDIUM,
            length = ExerciseLength.MEDIUM,
            language = "it",
            sections = listOf(
                ResolvedPromptSection(
                    id = "FULL_PROMPT",
                    content = "Prompt congelato"
                )
            )
        )
    }

    private fun buildContext(): PromptBuildContext {
        return PromptBuildContext(
            builderVersion = "1",
            templateId =
                PromptTemplateId.BANKING_ZERO_SHOT_V12.name,
            templateVersion = "12",
            libraryId = "phishing-awareness-library",
            libraryVersion = "1",
            librarySchemaVersion = 1
        )
    }

    private fun artifact(): PromptArtifact {
        return PromptArtifact(
            text = "Prompt congelato",
            metadata = PromptMetadata(
                buildContext = buildContext(),
                resolvedConfigurationId =
                    "BANKING_ZERO_SHOT_V12_MEDIUM_MEDIUM_IT",
                promptSha256 = "hash-di-test"
            )
        )
    }
}