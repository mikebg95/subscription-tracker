package dev.michaelgoldman.subscriptiontrackerbackend.integration;

import dev.michaelgoldman.subscriptiontrackerbackend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
public class SubscriptionApiIT {
    @Autowired
    RestTestClient restTestClient;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        String sqlClearAll = "TRUNCATE subscriptions RESTART IDENTITY";
        jdbcTemplate.update(sqlClearAll);

    }

    @Test
    void subscriptionLifecycle_createReadUpdateDelete_shouldSucceed() {
        // Create


        // Read

        // Update

        // Delete

        // Confirm gone
    }

}
