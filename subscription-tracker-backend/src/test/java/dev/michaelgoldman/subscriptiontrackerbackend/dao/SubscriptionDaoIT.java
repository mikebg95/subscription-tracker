package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.TestcontainersConfiguration;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class SubscriptionDaoIT {

    @Autowired
    SubscriptionDao subscriptionDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        void saveSubscription_whenValidSubscriptionDetailsProvided_shouldReturnSavedSubscription() {
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
        void saveSubscription_whenDuplicateSubscriptionNameProvided_shouldThrowSubscriptionAlreadyExistsException() {
            // Arrange
            Subscription firstSubscription = new Subscription("HBO Max", new BigDecimal("5.99"));
            Subscription duplicateSubscription =  new Subscription("HBO Max", new BigDecimal("13.99"));
            subscriptionDao.save(firstSubscription);

            // Act & Assert
            assertThatThrownBy(() -> subscriptionDao.save(duplicateSubscription))
                    .isInstanceOf(SubscriptionAlreadyExistsException.class);
        }

        @Test
        void saveSubscription_whenDuplicateSubscriptionNameDifferentCasesProvided_shouldThrowSubscriptionAlreadyExistsException() {
            // Arrange
            Subscription firstSubscription = new Subscription("HBO Max", new BigDecimal("5.99"));
            Subscription duplicateSubscription =  new Subscription("Hbo max", new BigDecimal("13.99"));
            subscriptionDao.save(firstSubscription);

            // Act & Assert
            assertThatThrownBy(() -> subscriptionDao.save(duplicateSubscription))
                    .isInstanceOf(SubscriptionAlreadyExistsException.class);
        }

        @Test
        void saveSubscription_whenNameExceedsLimit_shouldThrowDataIntegrityViolationException() {
            // Arrange
            String nameTooLong = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
            Subscription nameTooLongSubscription = new Subscription(nameTooLong, new BigDecimal("22.99"));

            // Act & Assert
            assertThatThrownBy(() -> subscriptionDao.save(nameTooLongSubscription))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void saveSubscription_whenPriceExceedsLimit_shouldThrowDataIntegrityViolationException() {
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

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        void findAllSubscriptions_whenSubscriptionsExist_shouldReturnListOfSubscriptionsOrderedById() {
            // Arrange
            Subscription sample1 = new Subscription("Netflix", new BigDecimal("11.99"));
            Subscription sample2 = new Subscription("Disney Plus", new BigDecimal("8.99"));
            Subscription sample3 = new Subscription("Amazon Prime", new BigDecimal("3.99"));

            String sql = "INSERT INTO subscriptions (name, price) VALUES (?, ?)";

            jdbcTemplate.update(sql, sample1.getName(), sample1.getPrice());
            jdbcTemplate.update(sql, sample2.getName(), sample2.getPrice());
            jdbcTemplate.update(sql, sample3.getName(), sample3.getPrice());

            // Act
            List<Subscription> fetchedSubscriptions = subscriptionDao.findAll();

            // Assert
            assertThat(fetchedSubscriptions).hasSize(3);
            assertThat(fetchedSubscriptions)
                    .extracting(Subscription::getName)
                    .containsExactly("Netflix", "Disney Plus", "Amazon Prime");
        }

        @Test
        void findAllSubscriptions_whenNoSubscriptionsExist_shouldReturnEmptyList() {
            // Act
            List<Subscription> fetchedSubscriptions = subscriptionDao.findAll();

            // Assert
            assertThat(fetchedSubscriptions).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        void findSubscriptionById_whenExistingIdProvided_shouldReturnCorrectSubscription() {
            // Arrange
            String sqlWithoutReturning = "INSERT INTO subscriptions (name, price) VALUES (?, ?)";
            String sql = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id";
            jdbcTemplate.update(sqlWithoutReturning, "Netflix", new BigDecimal("8.99"));
            Long savedId = jdbcTemplate.queryForObject(sql, Long.class, "Hbo Max", new BigDecimal("13.99"));

            // Act
            Optional<Subscription> subscriptionOptional = subscriptionDao.findById(savedId);

            // Assert
            assertThat(subscriptionOptional).isPresent();
            Subscription fetchedSubscription = subscriptionOptional.get();
            assertThat(fetchedSubscription.getId()).isEqualTo(savedId);
            assertThat(fetchedSubscription.getName()).isEqualTo("Hbo Max");
            assertThat(fetchedSubscription.getPrice()).isEqualByComparingTo(new BigDecimal("13.99"));
        }

        @Test
        void findSubscriptionById_whenNonExistingIdProvided_shouldReturnEmptyOptional() {
            // Act
            Optional<Subscription> subscriptionOptional = subscriptionDao.findById(999L);

            // Assert
            assertThat(subscriptionOptional).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {
        @Test
        void deleteSubscriptionById_whenExistingIdProvided_shouldRemoveSubscriptionAndReturnOne() {
            // Arrange
            String sql = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id";
            Long savedId1 = jdbcTemplate.queryForObject(sql, Long.class, "Hbo Max", new BigDecimal("8.99"));
            Long savedId2 = jdbcTemplate.queryForObject(sql, Long.class, "Netflix", new BigDecimal("8.99"));

            // Act
            int rowsDeleted = subscriptionDao.deleteById(savedId2);

            // Assert
            assertThat(rowsDeleted).isOne();
            String sqlCountById = "SELECT COUNT(*) FROM subscriptions WHERE id = ?";
            Long countDeleted = jdbcTemplate.queryForObject(sqlCountById, Long.class, savedId2);
            Long countNotDeleted = jdbcTemplate.queryForObject(sqlCountById, Long.class, savedId1);
            assertThat(countDeleted).isZero();
            assertThat(countNotDeleted).isOne();
        }

        @Test
        void deleteSubscriptionById_whenNonExistingIdProvided_shouldReturnZero() {
            // Act
            int rowsDeleted = subscriptionDao.deleteById(999L);

            // Assert
            assertThat(rowsDeleted).isZero();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        void updateSubscription_whenValidDetailsProvided_shouldUpdateSubscriptionAndReturnOne() {
            // Arrange
            String insertSql = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id";
            Long savedId = jdbcTemplate.queryForObject(insertSql, Long.class, "Netflix", new BigDecimal("8.99"));
            Subscription updateSub = new Subscription(savedId, "Amazon Prime", new BigDecimal("13.99"));

            // Act
            int rowsUpdated = subscriptionDao.update(updateSub);

            // Assert
            assertThat(rowsUpdated).isOne();
            String updatedName = jdbcTemplate.queryForObject("SELECT name FROM subscriptions WHERE id = ?", String.class, savedId);
            BigDecimal updatedPrice = jdbcTemplate.queryForObject("SELECT price FROM subscriptions WHERE id = ?", BigDecimal.class, savedId);
            assertThat(updatedName).isEqualTo("Amazon Prime");
            assertThat(updatedPrice).isEqualByComparingTo(new BigDecimal("13.99"));
        }

        @Test
        void updateSubscription_whenIdDoesNotExist_shouldReturnZero() {
            // Arrange
            Subscription updateSub = new Subscription(1L, "Amazon Prime", new BigDecimal("13.99"));

            // Act
            int rowsUpdated = subscriptionDao.update(updateSub);

            // Assert
            assertThat(rowsUpdated).isZero();
        }

        @Test
        void updateSubscription_whenNameAlreadyExists_shouldThrowSubscriptionAlreadyExistsException() {
            // Arrange
            String sqlInsertReturning = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id";
            String sqlInsert = "INSERT INTO subscriptions (name, price) VALUES (?, ?)";
            Long id1 = jdbcTemplate.queryForObject(sqlInsertReturning, Long.class, "Netflix", new BigDecimal("8.99"));
            jdbcTemplate.update(sqlInsert, "HBO Max", new BigDecimal("13.99"));

            // Act & Assert
            assertThatThrownBy(() -> subscriptionDao.update(new Subscription(id1, "HBO Max", new BigDecimal("19.99"))))
                    .isInstanceOf(SubscriptionAlreadyExistsException.class);
        }
    }
}