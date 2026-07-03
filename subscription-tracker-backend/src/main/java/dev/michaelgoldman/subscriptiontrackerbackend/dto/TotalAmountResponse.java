package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "The combined monthly price of all subscriptions.")
public record TotalAmountResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal amount) {}