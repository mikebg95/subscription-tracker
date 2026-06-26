package dev.michaelgoldman.subscriptiontrackerbackend.dto;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import java.math.BigDecimal;

public record SubscriptionResponse(
        Long id,
        String name,
        BigDecimal price) {

    public static SubscriptionResponse fromEntity(Subscription entity) {
        return new SubscriptionResponse(entity.getId(), entity.getName(), entity.getPrice());
    }
}