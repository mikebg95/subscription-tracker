package dev.michaelgoldman.subscriptiontrackerbackend.service;

import dev.michaelgoldman.subscriptiontrackerbackend.dao.SubscriptionDao;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionNotFoundException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    private final SubscriptionDao subscriptionDao;

    @Transactional
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
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    @Transactional
    public void deleteSubscriptionById(Long id) {
        int rowsDeleted = subscriptionDao.deleteById(id);
        if (rowsDeleted == 0) {
            throw new SubscriptionNotFoundException(id);
        }
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest subscriptionRequest) {
        Subscription subscriptionToUpdate = new Subscription(id, subscriptionRequest.name(), subscriptionRequest.price());
        int rowsUpdated = subscriptionDao.update(subscriptionToUpdate);
        if (rowsUpdated == 0) {
            throw new SubscriptionNotFoundException(id);
        }
        return SubscriptionResponse.fromEntity(subscriptionToUpdate);
    }
}
