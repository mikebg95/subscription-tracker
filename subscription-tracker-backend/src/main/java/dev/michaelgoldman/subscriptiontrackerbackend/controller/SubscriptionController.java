package dev.michaelgoldman.subscriptiontrackerbackend.controller;

import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        SubscriptionResponse subscriptionResponse = subscriptionService.createSubscription(subscriptionRequest);

        URI locationURI = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(subscriptionResponse.id())
                .toUri();

        return ResponseEntity
                .created(locationURI)
                .body(subscriptionResponse);
    }
}