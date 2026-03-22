package com.yupi.yuaicodemother.core.handler;

import cn.hutool.json.JSONUtil;
import com.yupi.yuaicodemother.ai.model.message.AiResponseMessage;
import com.yupi.yuaicodemother.ai.model.message.ToolExecutedMessage;
import com.yupi.yuaicodemother.ai.model.message.StreamMessageTypeEnum;
import com.yupi.yuaicodemother.ai.tools.FileWriteTool;
import com.yupi.yuaicodemother.ai.tools.ToolManager;
import com.yupi.yuaicodemother.model.entity.User;
import com.yupi.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.yupi.yuaicodemother.service.ChatHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonMessageStreamHandlerTest {

    @Test
    void handleShouldNotLeakWriteFileArgumentsToStreamOrHistory() {
        JsonMessageStreamHandler handler = new JsonMessageStreamHandler();
        ToolManager toolManager = mock(ToolManager.class);
        ReflectionTestUtils.setField(handler, "toolManager", toolManager);
        when(toolManager.getTool("writeFile")).thenReturn(new FileWriteTool());

        ChatHistoryService chatHistoryService = mock(ChatHistoryService.class);
        User loginUser = new User();
        loginUser.setId(123L);

        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage();
        toolExecutedMessage.setType(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        toolExecutedMessage.setName("writeFile");
        toolExecutedMessage.setArguments(JSONUtil.toJsonStr(JSONUtil.createObj()
                .set("relativeFilePath", "src/components/Footer.vue")
                .set("content", "<template>very long internal content</template>")));
        toolExecutedMessage.setResult("文件写入成功: src/components/Footer.vue");

        List<String> outputs = handler.handle(
                Flux.just(
                        JSONUtil.toJsonStr(new AiResponseMessage("开始生成")),
                        JSONUtil.toJsonStr(toolExecutedMessage),
                        JSONUtil.toJsonStr(new AiResponseMessage("，已完成"))
                ),
                chatHistoryService,
                1L,
                loginUser
        ).collectList().block();

        String mergedOutput = String.join("", outputs);
        assertTrue(mergedOutput.contains("文件写入成功: src/components/Footer.vue"));
        assertFalse(mergedOutput.contains("very long internal content"));
        assertFalse(mergedOutput.contains("\"relativeFilePath\""));

        ArgumentCaptor<String> historyCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatHistoryService).addChatMessage(
                eq(1L),
                historyCaptor.capture(),
                eq(ChatHistoryMessageTypeEnum.AI.getValue()),
                eq(123L)
        );
        String savedHistory = historyCaptor.getValue();
        assertTrue(savedHistory.contains("文件写入成功: src/components/Footer.vue"));
        assertFalse(savedHistory.contains("very long internal content"));
        assertFalse(savedHistory.contains("\"relativeFilePath\""));
    }
}
