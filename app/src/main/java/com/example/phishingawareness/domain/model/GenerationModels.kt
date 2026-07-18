package com.example.phishingawareness.domain.model

data class GenerationRequest(
    val scenarioId: String,
    val difficulty: String,
    val length: String
)

data class GeneratedEmail(
    val senderName: String,
    val senderAddress: String,
    val subject: String,
    val body: String,
    val presentIndicatorIds: Set<String>
)