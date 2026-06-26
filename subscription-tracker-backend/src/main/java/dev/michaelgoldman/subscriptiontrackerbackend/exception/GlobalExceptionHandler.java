package dev.michaelgoldman.subscriptiontrackerbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ProblemDetail handleSubscriptionAlreadyExists(SubscriptionAlreadyExistsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Duplicate Subscription");
        return problem;
    }
}
