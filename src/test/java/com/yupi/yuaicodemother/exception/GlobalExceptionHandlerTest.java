package com.yupi.yuaicodemother.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void runtimeExceptionHandlerShouldIgnoreSseResponseThatAlreadyUsesOutputStream() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/app/chat/gen/code");
        request.addHeader("Accept", "text/event-stream");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IllegalStateException("getOutputStream() has already been called for this response"));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        assertDoesNotThrow(() -> {
            Object result = handler.runtimeExceptionHandler(new RuntimeException("boom"));
            assertNull(result);
        });
    }
}
