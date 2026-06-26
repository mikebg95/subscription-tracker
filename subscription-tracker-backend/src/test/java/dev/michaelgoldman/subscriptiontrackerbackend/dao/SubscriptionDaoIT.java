package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.TestcontainersConfiguration;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SubscriptionDaoIT {

    @Autowired
    SubscriptionDao subscriptionDao;

    @Test
    void saveSubscription_whenProvidedValidSubscriptionDetails_shouldReturnSavedSubscriptionObject() {
        // Arrange
        Subscription newSubscription = new Subscription("Netflix", new BigDecimal("8.99"));

        // Act
        Subscription savedSubscription = subscriptionDao.save(newSubscription);

        // Assert
        assertThat(savedSubscription).isNotNull();
        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(savedSubscription.getName()).isEqualTo(newSubscription.getName());
        assertThat(savedSubscription.getPrice()).isEqualByComparingTo(newSubscription.getPrice());
    }

    @Test
    void saveSubscription_whenProvidedDuplicateSubscriptionName_shouldThrowDuplicateKeyException() {
        // Arrange
        Subscription firstSubscription = new Subscription("HBO Max", new BigDecimal("5.99"));
        Subscription duplicateSubscription =  new Subscription("HBO Max", new BigDecimal("13.99"));
        subscriptionDao.save(firstSubscription);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(duplicateSubscription))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void saveSubscription_whenProvidedDuplicateSubscriptionNameDifferentCases_shouldThrowDuplicateKeyException() {
        // Arrange
        Subscription firstSubscription = new Subscription("HBO Max", new BigDecimal("5.99"));
        Subscription duplicateSubscription =  new Subscription("Hbo max", new BigDecimal("13.99"));
        subscriptionDao.save(firstSubscription);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(duplicateSubscription))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void saveSubscription_whenNameInputExceedsLimit_shouldThrowDataIntegrityViolationException() {
        // Arrange
        String nameTooLong = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        Subscription nameTooLongSubscription = new Subscription(nameTooLong, new BigDecimal("22.99"));

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(nameTooLongSubscription))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveSubscription_whenPriceInputExceedsLimit_shouldThrowDataIntegrityViolationException() {
        // Arrange
        BigDecimal priceTooBig = new BigDecimal("123456789.99");
        Subscription priceTooBigSubscription = new Subscription("Claude", priceTooBig);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(priceTooBigSubscription))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveSubscription_whenNameIsNull_shouldThrowDataIntegrityViolationException() {
        // Arrange
        Subscription nameNullSubscription = new Subscription(null, new BigDecimal("8.99"));

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(nameNullSubscription))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveSubscription_whenPriceIsNull_shouldThrowDataIntegrityViolationException() {
        // Arrange
        Subscription priceNullSubscription = new Subscription("Netflix", null);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionDao.save(priceNullSubscription))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveSubscription_whenDangerousSqlInjected_shouldSaveSubscriptionAsUsual() {
        // Arrange
        String nameDangerousSqlInjection = "Netflix; DROP TABLE subscriptions; --";
        BigDecimal price = new BigDecimal("13.99");
        Subscription dangerousSubscription = new Subscription(nameDangerousSqlInjection, price);

        // Act
        Subscription savedSubscription = subscriptionDao.save(dangerousSubscription);

        // Assert
        assertThat(savedSubscription).isNotNull();
        assertThat(savedSubscription.getId()).isNotNull();
        assertThat(savedSubscription.getName()).isEqualTo(dangerousSubscription.getName());
        assertThat(savedSubscription.getPrice()).isEqualByComparingTo(dangerousSubscription.getPrice());
    }
}