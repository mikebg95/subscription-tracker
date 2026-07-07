package dev.michaelgoldman.subscriptiontrackerbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
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
    @Bean
    public OpenApiCustomizer globalErrorResponses() {
        Content problemContent = new Content().addMediaType(
                "application/problem+json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")));

        ApiResponse serverError = new ApiResponse()
                .description("Unexpected server error")
                .content(problemContent);

        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation ->
                        operation.getResponses()
                                .addApiResponse("500", serverError)));
    }
}
