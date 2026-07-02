package dev.michaelgoldman.subscriptiontrackerbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Subscription Tracker API",
                version = "1.0",
                description = "Backend services for tracking subscriptions"
        )
)
public class OpenApiConfig {
}
