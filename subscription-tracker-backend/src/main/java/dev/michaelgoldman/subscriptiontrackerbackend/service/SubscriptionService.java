package dev.michaelgoldman.subscriptiontrackerbackend.service;

import dev.michaelgoldman.subscriptiontrackerbackend.dao.SubscriptionDao;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionNotFoundException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final SubscriptionDao subscriptionDao;

    public SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest) {
        Subscription savedSubscription = subscriptionDao.save(subscriptionRequest.toEntity());
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    public List<SubscriptionResponse> getSubscriptions() {
        List<Subscription> fetchedSubscriptions = subscriptionDao.findAll();

        return fetchedSubscriptions
                .stream()
                .map(SubscriptionResponse::fromEntity)
                .toList();
    }

    public SubscriptionResponse getSubscriptionById(Long id) {
        return subscriptionDao.findById(id)
                .map(SubscriptionResponse::fromEntity)
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription found with id " + id));
    }
}
