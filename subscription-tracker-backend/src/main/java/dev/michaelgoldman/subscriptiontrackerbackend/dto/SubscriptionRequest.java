package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubscriptionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price) {

    public Subscription toEntity() {
        return new Subscription(this.name, this.price);
    }
}