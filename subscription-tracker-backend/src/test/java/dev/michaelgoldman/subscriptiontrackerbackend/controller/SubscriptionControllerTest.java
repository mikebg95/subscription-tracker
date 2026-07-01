package dev.michaelgoldman.subscriptiontrackerbackend.controller;

import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionNotFoundException;
import dev.michaelgoldman.subscriptiontrackerbackend.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    private static final Long SUBSCRIPTION_ID = 1L;
    private static final String SUBSCRIPTION_NAME = "Netflix";
    private static final BigDecimal SUBSCRIPTION_PRICE = new BigDecimal("8.99");
    private static final Long NON_EXISTING_SUBSCRIPTION_ID = 999L;
    private static final String VALID_REQUEST_BODY = """
            { "name": "Netflix", "price": 8.99 }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SubscriptionService subscriptionService;

    @Nested
    @DisplayName("POST /subscriptions")
    class CreateSubscription {
        @Test
        void createSubscription_whenValidDetailsProvided_shouldPassSubscriptionRequestToService() throws Exception {
            // Arrange
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse(SUBSCRIPTION_ID, SUBSCRIPTION_NAME, SUBSCRIPTION_PRICE);
            when(subscriptionService.createSubscription(any(SubscriptionRequest.class))).thenReturn(subscriptionResponse);

            // Act & Assert
            mockMvc.perform(postSubscription().content(VALID_REQUEST_BODY));

            ArgumentCaptor<SubscriptionRequest> captor = ArgumentCaptor.forClass(SubscriptionRequest.class);
            verify(subscriptionService).createSubscription(captor.capture());
            SubscriptionRequest subscriptionRequest = captor.getValue();

            assertThat(subscriptionRequest.name()).isEqualTo(SUBSCRIPTION_NAME);
            assertThat(subscriptionRequest.price()).isEqualByComparingTo(SUBSCRIPTION_PRICE);
        }

        @Test
        void createSubscription_whenValidDetailsProvided_shouldReturn201WithLocationAndSubscriptionResponse() throws Exception {
            // Arrange
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse(SUBSCRIPTION_ID, SUBSCRIPTION_NAME, SUBSCRIPTION_PRICE);
            when(subscriptionService.createSubscription(any(SubscriptionRequest.class))).thenReturn(subscriptionResponse);

            // Act & Assert
            mockMvc.perform(postSubscription().content(VALID_REQUEST_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/subscriptions/" + SUBSCRIPTION_ID)))
                    .andExpect(jsonPath("$.id").value(equalTo(SUBSCRIPTION_ID), Long.class))
                    .andExpect(jsonPath("$.name").value(SUBSCRIPTION_NAME))
                    .andExpect(jsonPath("$.price").value(comparesEqualTo(SUBSCRIPTION_PRICE), BigDecimal.class));
        }

        @Test
        void createSubscription_whenServiceThrowsSubscriptionAlreadyExistsException_shouldReturn409() throws Exception {
            // Arrange
            when(subscriptionService.createSubscription(any(SubscriptionRequest.class))).thenThrow(SubscriptionAlreadyExistsException.class);

            // Act & Assert
            mockMvc.perform(postSubscription().content(VALID_REQUEST_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
        }

        @Test
        void createSubscription_whenNoRequestBodyProvided_shouldReturn400() throws Exception {
            mockMvc.perform(postSubscription())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dev.michaelgoldman.subscriptiontrackerbackend.controller.SubscriptionControllerTest#invalidSubscriptionProvider")
        void createSubscription_whenInvalidInputsProvided_shouldReturn400(String testCase, String requestBody) throws Exception {
            mockMvc.perform(postSubscription().content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }

        @Test
        void createSubscription_whenMalformedJsonProvided_shouldReturn400() throws Exception {
            String malformedRequestBody = """
            { "name": , "price": 21.999 }
            """;
            mockMvc.perform(postSubscription().content(malformedRequestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }
    }

    @Nested
    @DisplayName("GET /subscriptions")
    class GetSubscriptions {
        @Test
        void getSubscriptions_whenSubscriptionsExist_shouldReturn200WithListOfSubscriptions() throws Exception {
            // Arrange
            when(subscriptionService.getSubscriptions()).thenReturn(
                    List.of(
                            new SubscriptionResponse(1L, "Netflix", new BigDecimal("11.99")),
                            new SubscriptionResponse(2L, "Disney Plus", new BigDecimal("8.99")),
                            new SubscriptionResponse(3L, "Amazon Prime", new BigDecimal("3.99"))
                    )
            );

            // Act & Assert
            mockMvc.perform(getSubscriptions())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].id", contains(1, 2, 3)))
                    .andExpect(jsonPath("$[*].name", contains("Netflix", "Disney Plus", "Amazon Prime")))
                    .andExpect(jsonPath("$[0].price").value(comparesEqualTo(new BigDecimal("11.99")), BigDecimal.class))
                    .andExpect(jsonPath("$[1].price").value(comparesEqualTo(new BigDecimal("8.99")), BigDecimal.class))
                    .andExpect(jsonPath("$[2].price").value(comparesEqualTo(new BigDecimal("3.99")), BigDecimal.class));
        }

        @Test
        void getSubscriptions_whenNoSubscriptionsExist_shouldReturn200WithEmptyList() throws Exception {
            // Arrange
            when(subscriptionService.getSubscriptions()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(getSubscriptions())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /subscriptions/{id}")
    class GetSubscriptionById {
        @Test
        void getSubscriptionById_whenExistingIdProvided_shouldReturn200WithSubscriptionResponse() throws Exception {
            // Arrange
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse(SUBSCRIPTION_ID, SUBSCRIPTION_NAME, SUBSCRIPTION_PRICE);
            when(subscriptionService.getSubscriptionById(SUBSCRIPTION_ID)).thenReturn(subscriptionResponse);

            // Act & Assert
            mockMvc.perform(getSubscription(SUBSCRIPTION_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(equalTo(subscriptionResponse.id()), Long.class))
                    .andExpect(jsonPath("$.name").value(subscriptionResponse.name()))
                    .andExpect(jsonPath("$.price").value(comparesEqualTo(subscriptionResponse.price()), BigDecimal.class));
        }

        @Test
        void getSubscriptionById_whenNonExistingIdProvided_shouldReturn404() throws Exception {
            // Arrange
            when(subscriptionService.getSubscriptionById(NON_EXISTING_SUBSCRIPTION_ID)).thenThrow(SubscriptionNotFoundException.class);

            // Act & Assert
            mockMvc.perform(getSubscription(NON_EXISTING_SUBSCRIPTION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "-1", "0", "1.5", "abc" })
        void getSubscriptionById_whenInvalidIdProvided_shouldReturn400(String subscriptionId) throws Exception {
            // Act & Assert
            mockMvc.perform(getSubscription(subscriptionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }
    }

    @Nested
    @DisplayName("DELETE /subscriptions/{id}")
    class DeleteSubscriptionById {
        @Test
        void deleteSubscriptionById_whenExistingIdProvided_shouldReturn204() throws Exception {
            // Act & Assert
            mockMvc.perform(deleteSubscription(SUBSCRIPTION_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteSubscriptionById_whenExistingIdProvided_shouldPassSubscriptionIdToService() throws Exception {
            // Act
            mockMvc.perform(deleteSubscription(SUBSCRIPTION_ID));

            // Assert
            ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
            verify(subscriptionService).deleteSubscriptionById(captor.capture());
            Long passedId = captor.getValue();
            assertThat(passedId).isEqualTo(SUBSCRIPTION_ID);
        }

        @Test
        void deleteSubscriptionById_whenNonExistingIdProvided_shouldReturn404() throws Exception {
            // Arrange
            doThrow(SubscriptionNotFoundException.class).when(subscriptionService).deleteSubscriptionById(NON_EXISTING_SUBSCRIPTION_ID);

            // Act & Assert
            mockMvc.perform(deleteSubscription(NON_EXISTING_SUBSCRIPTION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "-1", "0", "1.5", "abc" })
        void deleteSubscriptionById_whenInvalidIdProvided_shouldReturn400(String subscriptionId) throws Exception {
            // Act & Assert
            mockMvc.perform(deleteSubscription(subscriptionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }
    }

    @Nested
    @DisplayName("UPDATE /subscriptions/{id}")
    class UpdateSubscription {
        @Test
        void updateSubscription_whenValidDetailsProvided_shouldPassSubscriptionIdAndRequestToService() throws Exception {
            // Arrange
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse(SUBSCRIPTION_ID, SUBSCRIPTION_NAME, SUBSCRIPTION_PRICE);
            when(subscriptionService.updateSubscription(any(Long.class), any(SubscriptionRequest.class))).thenReturn(subscriptionResponse);

            // Act
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID).content(VALID_REQUEST_BODY));

            // Assert
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<SubscriptionRequest> requestCaptor = ArgumentCaptor.forClass(SubscriptionRequest.class);

            verify(subscriptionService).updateSubscription(idCaptor.capture(), requestCaptor.capture());
            Long id = idCaptor.getValue();
            SubscriptionRequest request = requestCaptor.getValue();
            assertThat(id).isEqualTo(SUBSCRIPTION_ID);
            assertThat(request.name()).isEqualTo(SUBSCRIPTION_NAME);
            assertThat(request.price()).isEqualByComparingTo(SUBSCRIPTION_PRICE);
        }

        @Test
        void updateSubscription_whenValidDetailsProvided_shouldReturn200WithSubscriptionResponse() throws Exception {
            // Arrange
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse(SUBSCRIPTION_ID, SUBSCRIPTION_NAME, SUBSCRIPTION_PRICE);
            when(subscriptionService.updateSubscription(any(Long.class), any(SubscriptionRequest.class))).thenReturn(subscriptionResponse);

            // Act & Assert
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID).content(VALID_REQUEST_BODY))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(equalTo(SUBSCRIPTION_ID), Long.class))
                    .andExpect(jsonPath("$.name").value(SUBSCRIPTION_NAME))
                    .andExpect(jsonPath("$.price").value(comparesEqualTo(SUBSCRIPTION_PRICE), BigDecimal.class));
        }

        @Test
        void updateSubscription_whenSubscriptionDoesNotExist_shouldReturn404() throws Exception {
            // Arrange
            when(subscriptionService.updateSubscription(any(Long.class), any(SubscriptionRequest.class)))
                    .thenThrow(SubscriptionNotFoundException.class);

            // Act & Assert
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID).content(VALID_REQUEST_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));
        }

        @Test
        void updateSubscription_whenNameAlreadyExists_shouldReturn409() throws Exception {
            // Arrange
            when(subscriptionService.updateSubscription(any(Long.class), any(SubscriptionRequest.class)))
                    .thenThrow(SubscriptionAlreadyExistsException.class);

            // Act & Assert
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID).content(VALID_REQUEST_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "-1", "0", "1.5", "abc" })
        void updateSubscription_whenInvalidIdProvided_shouldReturn400(String invalidId) throws Exception {
            // Act & Assert
            mockMvc.perform(putSubscription(invalidId).content(VALID_REQUEST_BODY))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dev.michaelgoldman.subscriptiontrackerbackend.controller.SubscriptionControllerTest#invalidSubscriptionProvider")
        void updateSubscription_whenInvalidInputsProvided_shouldReturn400(String testCase, String requestBody) throws Exception {
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID).content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }

        @Test
        void updateSubscription_whenNoRequestBodyProvided_shouldReturn400() throws Exception {
            mockMvc.perform(putSubscription(SUBSCRIPTION_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

            verifyNoInteractions(subscriptionService);
        }
    }

    private static Stream<Arguments> invalidSubscriptionProvider() {
        return Stream.of(
                Arguments.of(
                        "name is blank",
                        """
                        { "name": "", "price": 8.99 }
                        """
                ),
                Arguments.of(
                        "name is only spaces",
                        """
                        { "name": "     ", "price": 8.99 }
                        """
                ),
                Arguments.of(
                        "name is null",
                        """
                        { "price": 8.99 }
                        """
                ),
                Arguments.of(
                        "price is null",
                        """
                        { "name": "Netflix" }
                        """
                ),
                Arguments.of(
                        "price is negative",
                        """
                        { "name": "Netflix", "price": -9.99 }
                        """
                ),
                Arguments.of(
                        "name is too long",
                        """
                        { "name": "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", "price": 8.99 }
                        """
                ),
                Arguments.of(
                        "price exceeds limit",
                        """
                        { "name": "Netflix", "price": 123456789.99 }
                        """
                ),
                Arguments.of(
                        "price has too many decimals",
                        """
                        { "name": "Netflix", "price": 21.999 }
                        """
                )
        );
    }

    private MockHttpServletRequestBuilder postSubscription() {
        return post("/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder getSubscriptions() {
        return get("/subscriptions")
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder getSubscription(Object subscriptionId) {
        return get("/subscriptions/{id}", subscriptionId)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder deleteSubscription(Object subscriptionId) {
        return delete("/subscriptions/{id}", subscriptionId)
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder putSubscription(Object subscriptionId) {
        return put("/subscriptions/{id}", subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
