package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Data required to create or update a subscription.")
public record SubscriptionRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "Name of the subscription, for example \"Netflix\". Required, 1–100 characters.")
        String name,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 8, fraction = 2)
        @Schema(description = "Recurring monthly price. Required, positive, up to 8 integer digits and 2 decimal places.")
        BigDecimal price) {

    public Subscription toEntity() {
        return new Subscription(this.name, this.price);
    }
}