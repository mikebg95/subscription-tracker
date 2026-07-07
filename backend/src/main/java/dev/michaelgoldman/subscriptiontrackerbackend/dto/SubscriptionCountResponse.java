package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The total number of subscriptions.")
public record SubscriptionCountResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long count) {}
