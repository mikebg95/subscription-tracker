package dev.michaelgoldman.subscriptiontrackerbackend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Subscription {
    private Long id;
    private String name;
    private BigDecimal price;

    public Subscription(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
}
