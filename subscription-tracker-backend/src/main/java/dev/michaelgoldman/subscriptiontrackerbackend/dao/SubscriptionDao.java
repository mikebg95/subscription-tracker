package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;

import java.util.List;

public interface SubscriptionDao {
    Subscription save(Subscription subscription);
    List<Subscription> findAll();
}
