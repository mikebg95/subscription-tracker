package dev.michaelgoldman.subscriptiontrackerbackend.controller;

import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionCountResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        SubscriptionResponse subscriptionResponse = subscriptionService.createSubscription(subscriptionRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(subscriptionResponse.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(subscriptionResponse);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable @Positive Long id) {
        SubscriptionResponse subscriptionResponse = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscriptionResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriptionById(@PathVariable @Positive Long id) {
        subscriptionService.deleteSubscriptionById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(@PathVariable @Positive Long id, @Valid @RequestBody SubscriptionRequest subscriptionRequest) {
        SubscriptionResponse subscriptionResponse = subscriptionService.updateSubscription(id, subscriptionRequest);
        return ResponseEntity.ok(subscriptionResponse);
    }

    @GetMapping("/count")
    public ResponseEntity<SubscriptionCountResponse> countSubscriptions() {
        long count = subscriptionService.countSubscriptions();
        SubscriptionCountResponse countResponse = new SubscriptionCountResponse(count);
        return ResponseEntity.ok(countResponse);
    }
}