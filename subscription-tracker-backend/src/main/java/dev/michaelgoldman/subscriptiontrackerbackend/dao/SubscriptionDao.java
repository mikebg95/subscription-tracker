package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;

public interface SubscriptionDao {
    Subscription save(Subscription subscription);
}
