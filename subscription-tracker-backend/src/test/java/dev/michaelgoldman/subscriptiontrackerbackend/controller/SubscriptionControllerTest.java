package dev.michaelgoldman.subscriptiontrackerbackend.controller;

import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionRequest;
import dev.michaelgoldman.subscriptiontrackerbackend.dto.SubscriptionResponse;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionNotFoundException;
import dev.michaelgoldman.subscriptiontrackerbackend.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    private static final Long SUBSCRIPTION_ID = 1L;
    private static final String SUBSCRIPTION_NAME = "Netflix";
    private static final BigDecimal SUBSCRIPTION_PRICE = new BigDecimal("8.99");
    private static final String VALID_REQUEST_BODY = """
            { "name": "Netflix", "price": 8.99 }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SubscriptionService subscriptionService;

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
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void createSubscription_whenNoRequestBodyProvided_shouldReturn400() throws Exception {
        mockMvc.perform(postSubscription())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSubscriptionProvider")
    void createSubscription_whenInvalidInputsProvided_shouldReturn400(String testCase, String requestBody) throws Exception {
        mockMvc.perform(postSubscription().content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(subscriptionService);
    }

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
                .andExpect(jsonPath("$[*].price", contains(11.99, 8.99, 3.99)));
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

    @Test
    void getSubscriptionById_whenExistingIdProvided_shouldReturn200WithSubscription() throws Exception {
        // Arrange
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse(1L, "Netflix", new BigDecimal("11.99"));
        when(subscriptionService.getSubscriptionById(1L)).thenReturn(subscriptionResponse);

        // Act & Assert
        mockMvc.perform(getSubscription("1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(equalTo(subscriptionResponse.id()), Long.class))
                .andExpect(jsonPath("$.name").value(subscriptionResponse.name()))
                .andExpect(jsonPath("$.price").value(comparesEqualTo(subscriptionResponse.price()), BigDecimal.class));

        verify(subscriptionService).getSubscriptionById(1L);
    }

    @Test
    void getSubscriptionById_whenNonExistingIdProvided_shouldReturn404() throws Exception {
        // Arrange
        when(subscriptionService.getSubscriptionById(999L)).thenThrow(SubscriptionNotFoundException.class);

        // Act & Assert
        mockMvc.perform(getSubscription("999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "0", "abc" })
    void getSubscriptionById_whenInvalidIdProvided_shouldReturn400(String subscriptionId) throws Exception {
        // Act & Assert
        mockMvc.perform(getSubscription(subscriptionId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(subscriptionService);
    }

    @Test
    void deleteSubscriptionById_whenExistingIdProvided_shouldReturn204() throws Exception {
        // Act & Assert
        mockMvc.perform(deleteSubscription("1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSubscriptionById_whenExistingIdProvided_shouldCallServiceMethod() throws Exception {
        // Act
        mockMvc.perform(deleteSubscription("1"));

        // Assert
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(subscriptionService).deleteSubscriptionById(captor.capture());
        Long passedId = captor.getValue();
        assertThat(passedId).isEqualTo(1L);
    }

    @Test
    void deleteSubscriptionById_whenNonExistingIdProvided_shouldReturn404() throws Exception {
        // Arrange
        doThrow(SubscriptionNotFoundException.class).when(subscriptionService).deleteSubscriptionById(999L);

        // Act & Assert
        mockMvc.perform(deleteSubscription("999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1", "0", "abc" })
    void deleteSubscriptionById_whenInvalidIdProvided_shouldReturn400(String subscriptionId) throws Exception {
        // Act & Assert
        mockMvc.perform(deleteSubscription(subscriptionId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(subscriptionService);
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
                ),
                Arguments.of(
                        "name not formatted right",
                        """
                        { "name": , "price": 21.999 }
                        """
                ),
                Arguments.of(
                        "price not formatted right",
                        """
                        { "name": "Netflix", "price": }
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
        return get("/subscriptions").accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder getSubscription(String subscriptionId) {
        return get("/subscriptions/" + subscriptionId).accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder deleteSubscription(String subscriptionId) {
        return delete("/subscriptions/" + subscriptionId).accept(MediaType.APPLICATION_JSON);
    }
}
