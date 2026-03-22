package com.yupi.yuaicodemother.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AiModelMonitorListenerTest {

    private final AiModelMetricsCollector aiModelMetricsCollector = mock(AiModelMetricsCollector.class);

    @AfterEach
    void tearDown() {
        MonitorContextHolder.clearContext();
    }

    @Test
    void onRequestShouldIgnoreMissingMonitorContext() {
        AiModelMonitorListener listener = new AiModelMonitorListener();
        ReflectionTestUtils.setField(listener, "aiModelMetricsCollector", aiModelMetricsCollector);
        ChatModelRequestContext requestContext = mock(ChatModelRequestContext.class, RETURNS_DEEP_STUBS);
        Map<Object, Object> attributes = new HashMap<>();
        when(requestContext.attributes()).thenReturn(attributes);

        assertDoesNotThrow(() -> listener.onRequest(requestContext));
        verifyNoInteractions(aiModelMetricsCollector);
    }

    @Test
    void onResponseShouldIgnoreMissingMonitorContextFromAttributes() {
        AiModelMonitorListener listener = new AiModelMonitorListener();
        ReflectionTestUtils.setField(listener, "aiModelMetricsCollector", aiModelMetricsCollector);
        ChatModelResponseContext responseContext = mock(ChatModelResponseContext.class, RETURNS_DEEP_STUBS);
        Map<Object, Object> attributes = new HashMap<>();
        attributes.put("request_start_time", Instant.now());
        when(responseContext.attributes()).thenReturn(attributes);

        assertDoesNotThrow(() -> listener.onResponse(responseContext));
        verifyNoInteractions(aiModelMetricsCollector);
    }

    @Test
    void onErrorShouldIgnoreMissingMonitorContext() {
        AiModelMonitorListener listener = new AiModelMonitorListener();
        ReflectionTestUtils.setField(listener, "aiModelMetricsCollector", aiModelMetricsCollector);
        ChatModelErrorContext errorContext = mock(ChatModelErrorContext.class, RETURNS_DEEP_STUBS);
        when(errorContext.attributes()).thenReturn(new HashMap<>());

        assertDoesNotThrow(() -> listener.onError(errorContext));
        verifyNoInteractions(aiModelMetricsCollector);
    }
}
