package dev.michaelgoldman.subscriptiontrackerbackend.exception;

public class SubscriptionAlreadyExistsException extends RuntimeException {
    public SubscriptionAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
