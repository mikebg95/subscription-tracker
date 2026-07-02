package dev.michaelgoldman.subscriptiontrackerbackend.integration;

import dev.michaelgoldman.subscriptiontrackerbackend.TestcontainersConfiguration;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
class SubscriptionApiIT {
    @Autowired
    RestTestClient restTestClient;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.execute("TRUNCATE subscriptions RESTART IDENTITY");
    }

    @Test
    void subscriptionLifecycle_createReadUpdateDelete_shouldSucceed() {
        // Create
        SubscriptionRequest createRequest = new SubscriptionRequest("Netflix", new BigDecimal("8.99"));

        SubscriptionResponse createResponse = restTestClient
                .post()
                .uri("/api/v1/subscriptions")
                .body(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SubscriptionResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(createResponse).isNotNull();
        Long createdId = createResponse.id();

        // Read
        SubscriptionResponse readResponse = restTestClient
                .get()
                .uri("/api/v1/subscriptions/{id}", createdId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(readResponse).isNotNull();
        assertThat(readResponse.id()).isEqualTo(createdId);
        assertThat(readResponse.name()).isEqualTo("Netflix");
        assertThat(readResponse.price()).isEqualByComparingTo(new BigDecimal("8.99"));

        // Update
        SubscriptionRequest updateRequest = new SubscriptionRequest("Amazon Prime", new BigDecimal("11.99"));

        SubscriptionResponse updateResponse = restTestClient
                .put()
                .uri("/api/v1/subscriptions/{id}", createdId)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updateResponse).isNotNull();
        assertThat(updateResponse.id()).isEqualTo(createdId);
        assertThat(updateResponse.name()).isEqualTo("Amazon Prime");
        assertThat(updateResponse.price()).isEqualByComparingTo(new BigDecimal("11.99"));

        // Delete
        restTestClient
                .delete()
                .uri("/api/v1/subscriptions/{id}", createdId)
                .exchange()
                .expectStatus().isNoContent();

        // Confirm gone
        ProblemDetail problemDetail = restTestClient
                .get()
                .uri("/api/v1/subscriptions/{id}", createdId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Subscription not found");
        assertThat(problemDetail.getDetail()).isEqualTo("No subscription found with id " + createdId + ".");
    }
}
