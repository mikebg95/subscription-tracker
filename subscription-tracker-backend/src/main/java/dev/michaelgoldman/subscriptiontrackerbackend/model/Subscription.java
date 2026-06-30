package dev.michaelgoldman.subscriptiontrackerbackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Subscription {
    private final Long id;
    private final String name;
    private final BigDecimal price;

    public Subscription(String name, BigDecimal price) {
        this(null, name, price);
    }
}
