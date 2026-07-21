package com.example.phishingawareness.generation.prompt

import com.example.phishingawareness.domain.model.PromptParameterResolutionIssue
import com.example.phishingawareness.domain.model.PromptParameterResolutionIssueCode
import com.example.phishingawareness.domain.model.PromptParameterResolutionRequest
import com.example.phishingawareness.domain.model.PromptParameterResolutionResult
import com.example.phishingawareness.domain.model.ResolvedPromptParameters
import com.example.phishingawareness.domain.model.RuntimePromptProfile
import com.example.phishingawareness.domain.prompt.PromptParameterResolver
import com.example.phishingawareness.domain.prompt.RuntimePromptProfileCatalog
import com.example.phishingawareness.domain.repository.LibraryRepository

class DeterministicPromptParameterResolver(
    private val profileCatalog: RuntimePromptProfileCatalog,
    private val libraryRepository: LibraryRepository
) : PromptParameterResolver {

    override fun resolve(
        request: PromptParameterResolutionRequest
    ): PromptParameterResolutionResult {
        val requestIssues = validateRequest(request)

        if (requestIssues.isNotEmpty()) {
            return PromptParameterResolutionResult.Failure(
                issues = requestIssues
            )
        }

        val configuration = request.userConfiguration

        val profile =
            profileCatalog.get(
                scenario = configuration.scenario,
                difficulty = configuration.difficulty,
                length = configuration.length
            )
                ?: return PromptParameterResolutionResult.Failure(
                    issues = listOf(
                        PromptParameterResolutionIssue(
                            code =
                                PromptParameterResolutionIssueCode
                                    .PROFILE_NOT_FOUND,
                            field = "userConfiguration",
                            details =
                                "Nessun profilo disponibile per " +
                                        "${configuration.scenario}, " +
                                        "${configuration.difficulty}, " +
                                        "${configuration.length}."
                        )
                    )
                )

        return resolveProfile(
            request = request,
            profile = profile
        )
    }

    private fun validateRequest(
        request: PromptParameterResolutionRequest
    ): List<PromptParameterResolutionIssue> {
        val issues =
            mutableListOf<PromptParameterResolutionIssue>()

        if (request.configurationId.isBlank()) {
            issues +=
                PromptParameterResolutionIssue(
                    code =
                        PromptParameterResolutionIssueCode
                            .MISSING_CONFIGURATION_ID,
                    field = "configurationId"
                )
        }

        if (request.language.isBlank()) {
            issues +=
                PromptParameterResolutionIssue(
                    code =
                        PromptParameterResolutionIssueCode
                            .MISSING_LANGUAGE,
                    field = "language"
                )
        }

        return issues
    }

    private fun resolveProfile(
        request: PromptParameterResolutionRequest,
        profile: RuntimePromptProfile
    ): PromptParameterResolutionResult {
        val indicatorPromptIds = mutableListOf<String>()
        val issues =
            mutableListOf<PromptParameterResolutionIssue>()

        profile.requiredIndicatorIds.forEach { indicatorId ->
            val indicator =
                libraryRepository.getIndicatorById(indicatorId)

            if (indicator == null) {
                issues +=
                    PromptParameterResolutionIssue(
                        code =
                            PromptParameterResolutionIssueCode
                                .INDICATOR_NOT_FOUND,
                        field = "requiredIndicatorIds",
                        details = indicatorId
                    )

                return@forEach
            }

            val scenarioId = profile.scenario.name

            if (
                !indicator.enabled ||
                !indicator.observable ||
                scenarioId !in indicator.scenarios
            ) {
                issues +=
                    PromptParameterResolutionIssue(
                        code =
                            PromptParameterResolutionIssueCode
                                .INDICATOR_NOT_AVAILABLE_FOR_SCENARIO,
                        field = "requiredIndicatorIds",
                        details = indicatorId
                    )

                return@forEach
            }

            indicatorPromptIds += indicator.promptId
        }

        if (issues.isNotEmpty()) {
            return PromptParameterResolutionResult.Failure(
                issues = issues
            )
        }

        return PromptParameterResolutionResult.Success(
            parameters = ResolvedPromptParameters(
                configurationId = request.configurationId,
                profileId = profile.id,
                scenario = profile.scenario,
                difficulty = profile.difficulty,
                length = profile.length,
                language = request.language,
                scenarioLabel = profile.scenarioLabel,
                pretext = profile.pretext,
                impersonatedIdentity =
                    profile.impersonatedIdentity,
                brandName = profile.brandName,
                ctaType = profile.ctaType,
                requiredIndicatorPromptIds =
                    indicatorPromptIds,
                credibilityElements =
                    profile.credibilityElements
            )
        )
    }
}