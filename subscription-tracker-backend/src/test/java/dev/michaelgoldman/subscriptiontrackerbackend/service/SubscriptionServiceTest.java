package dev.michaelgoldman.subscriptiontrackerbackend.service;

import dev.michaelgoldman.subscriptiontrackerbackend.dao.SubscriptionDao;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    SubscriptionService subscriptionService;

    @Mock
    SubscriptionDao subscriptionDao;

    @Test
    void createSubscription_whenValidSubscriptionRequestPassed_shouldPassSubscriptionToDao() {
        // Arrange
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("Spotify", new BigDecimal("11.99"));
        Subscription subscription = new Subscription(1L, "Spotify", new BigDecimal("11.99"));
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        when(subscriptionDao.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        subscriptionService.createSubscription(subscriptionRequest);

        // Assert
        verify(subscriptionDao).save(captor.capture());
        Subscription passed = captor.getValue();
        assertThat(passed.getId()).isNull();
        assertThat(passed.getName()).isEqualTo(subscriptionRequest.name());
        assertThat(passed.getPrice()).isEqualByComparingTo(subscriptionRequest.price());
    }

    @Test
    void createSubscription_whenValidSubscriptionRequestPassed_shouldReturnMappedResponse() {
        // Arrange
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("Spotify", new BigDecimal("11.99"));
        Subscription subscription = new Subscription(1L, "Netflix", new BigDecimal("8.99"));
        when(subscriptionDao.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        SubscriptionResponse subscriptionResponse = subscriptionService.createSubscription(subscriptionRequest);

        // Assert
        assertThat(subscriptionResponse.id()).isEqualTo(subscription.getId());
        assertThat(subscriptionResponse.name()).isEqualTo(subscription.getName());
        assertThat(subscriptionResponse.price()).isEqualByComparingTo(subscription.getPrice());
    }

    @Test
    void createSubscription_whenProvidedDuplicateName_shouldThrowSubscriptionAlreadyExistsException() {
        // Arrange
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest("Spotify", new BigDecimal("11.99"));
        when(subscriptionDao.save(any(Subscription.class))).thenThrow(DuplicateKeyException.class);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionService.createSubscription(subscriptionRequest))
                .isInstanceOf(SubscriptionAlreadyExistsException.class);
    }

    @Test
    void getSubscriptions_whenListOfSubscriptionsFetched_shouldReturnMappedSubscriptionResponses() {
        // Arrange
        List<Subscription> fetchedSubscriptions = List.of(
                new Subscription(1L, "Netflix", new BigDecimal("11.99")),
                new Subscription(2L, "Disney Plus", new BigDecimal("8.99")),
                new Subscription(3L, "Amazon Prime", new BigDecimal("3.99"))
        );
        when(subscriptionDao.findAll()).thenReturn(fetchedSubscriptions);

        // Act
        List<SubscriptionResponse> subscriptionResponses = subscriptionService.getSubscriptions();

        // Assert
        assertThat(subscriptionResponses)
                .extracting(SubscriptionResponse::id, SubscriptionResponse::name, SubscriptionResponse::price)
                .containsExactly(
                        tuple(1L, "Netflix", new BigDecimal("11.99")),
                        tuple(2L, "Disney Plus", new BigDecimal("8.99")),
                        tuple(3L, "Amazon Prime", new BigDecimal("3.99"))
                );
    }

    @Test
    void getSubscriptions_whenEmptyListFetched_shouldReturnEmptyList() {
        // Arrange
        when(subscriptionDao.findAll()).thenReturn(List.of());

        // Act
        List<SubscriptionResponse> subscriptionResponses = subscriptionService.getSubscriptions();

        // Assert
        assertThat(subscriptionResponses).isEmpty();
    }
}
