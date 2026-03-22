package com.yupi.yuaicodemother.config;

import com.yupi.yuaicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class ReasoningStreamingChatModelConfigTest {

    @Test
    void reasoningStreamingChatModelPrototypeShouldEnableStrictToolCalling() throws Exception {
        ReasoningStreamingChatModelConfig config = new ReasoningStreamingChatModelConfig();
        ReflectionTestUtils.setField(config, "aiModelMonitorListener", mock(AiModelMonitorListener.class));
        config.setApiKey("test-key");
        config.setBaseUrl("https://example.com");
        config.setModelName("test-model");
        config.setMaxTokens(1024);
        config.setTemperature(0.1);
        config.setLogRequests(false);
        config.setLogResponses(false);

        StreamingChatModel model = config.reasoningStreamingChatModelPrototype();
        OpenAiStreamingChatModel openAiModel = assertInstanceOf(OpenAiStreamingChatModel.class, model);

        assertEquals(Boolean.TRUE, ReflectionTestUtils.getField(openAiModel, "strictJsonSchema"));
        assertEquals(Boolean.TRUE, ReflectionTestUtils.getField(openAiModel, "strictTools"));

        Object defaultRequestParameters = openAiModel.defaultRequestParameters();
        Method parallelToolCallsMethod = defaultRequestParameters.getClass().getMethod("parallelToolCalls");
        assertFalse((Boolean) parallelToolCallsMethod.invoke(defaultRequestParameters));
    }
}
