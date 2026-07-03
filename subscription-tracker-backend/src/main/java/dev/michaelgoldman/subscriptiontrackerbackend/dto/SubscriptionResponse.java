package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "A subscription with its server-generated id, name and monthly price.")
public record SubscriptionResponse(
        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Server-generated unique identifier of the subscription."
        ) Long id,
        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Name of the subscription, for example \"Netflix\"."
        ) String name,
        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Recurring monthly price of the subscription."
        ) BigDecimal price) {

    public static SubscriptionResponse fromEntity(Subscription entity) {
        return new SubscriptionResponse(entity.getId(), entity.getName(), entity.getPrice());
    }
}