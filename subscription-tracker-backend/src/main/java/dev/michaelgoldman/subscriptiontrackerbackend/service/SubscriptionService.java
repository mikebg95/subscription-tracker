package dev.michaelgoldman.subscriptiontrackerbackend.service;

import dev.michaelgoldman.subscriptiontrackerbackend.dao.SubscriptionDao;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final SubscriptionDao subscriptionDao;

    public SubscriptionResponse createSubscription(SubscriptionRequest subscriptionRequest) {
        Subscription subscription = subscriptionRequest.toEntity();

        try {
            Subscription savedSubscription = subscriptionDao.save(subscription);
            return SubscriptionResponse.fromEntity(savedSubscription);
        } catch (DuplicateKeyException e) {
            throw new SubscriptionAlreadyExistsException("A subscription with that name already exists.", e);
        }
    }

    public List<SubscriptionResponse> getSubscriptions() {
        List<Subscription> fetchedSubscriptions = subscriptionDao.findAll();

        return fetchedSubscriptions
                .stream()
                .map(SubscriptionResponse::fromEntity)
                .toList();
    }
}
