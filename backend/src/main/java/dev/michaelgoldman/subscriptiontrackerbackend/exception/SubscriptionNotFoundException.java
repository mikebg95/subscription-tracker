package dev.michaelgoldman.subscriptiontrackerbackend.exception;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(Long id) {
        super("No subscription found with id " + id + ".");
    }
}
