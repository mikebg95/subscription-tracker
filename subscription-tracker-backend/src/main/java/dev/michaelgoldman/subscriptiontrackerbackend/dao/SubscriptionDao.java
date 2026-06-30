package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionDao {
    Subscription save(Subscription subscription);
    List<Subscription> findAll();
    Optional<Subscription> findById(Long id);
    int deleteById(Long id);
}
