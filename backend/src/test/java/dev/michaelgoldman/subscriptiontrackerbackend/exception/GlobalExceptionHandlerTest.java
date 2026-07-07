package dev.michaelgoldman.subscriptiontrackerbackend.exception;

import dev.michaelgoldman.subscriptiontrackerbackend.controller.SubscriptionController;
import dev.michaelgoldman.subscriptiontrackerbackend.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class GlobalExceptionHandlerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SubscriptionService subscriptionService;

    @Test
    void whenUnhandledExceptionThrown_shouldReturn500WithGenericProblemDetails() throws Exception {
        when(subscriptionService.getSubscriptionById(anyLong()))
                .thenThrow(new RuntimeException("database connection lost"));

        mockMvc.perform(get("/api/v1/subscriptions/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.detail", not(containsString("database connection lost"))));
    }
}
