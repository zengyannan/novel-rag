package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.GenerateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerationService 单元测试
 * 使用模拟实现测试
 */
class GenerationServiceTest {

    private MockGenerationService generationService;

    @BeforeEach
    void setUp() {
        generationService = new MockGenerationService();
    }

    @Test
    void testGenerateWithValidInput() {
        // Given
        String prompt = "写一段战斗场景";
        String context = "主角是一位剑客";

        // When
        GenerateResult result = generationService.generate(prompt, context);

        // Then
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getWordCount() > 0);
        assertEquals("qwen-max-longcontext", result.getModel());
    }

    @Test
    void testGenerateWithDefaultParameters() {
        // Given
        String prompt = "测试提示词";
        String context = "测试上下文";

        // When
        GenerateResult result = generationService.generate(prompt, context, 2000, 0.8);

        // Then
        assertNotNull(result);
        assertTrue(result.getWordCount() > 0);
    }

    @Test
    void testGenerateWithEmptyPrompt() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            generationService.generate("", "context");
        });
    }

    @Test
    void testGenerateWithNullPrompt() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            generationService.generate(null, "context");
        });
    }

    @Test
    void testGenerateWithNullContext() {
        // Given
        String prompt = "测试";

        // When
        GenerateResult result = generationService.generate(prompt, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateWithEmptyContext() {
        // Given
        String prompt = "测试";

        // When
        GenerateResult result = generationService.generate(prompt, "");

        // Then
        assertNotNull(result);
    }

    @Test
    void testStreamGeneration() {
        // Given
        String prompt = "测试流式生成";
        String context = "上下文";
        StringBuilder receivedContent = new StringBuilder();

        // When
        generationService.generateStream(prompt, context, 1000, 0.8, new GenerationService.StreamListener() {
            @Override
            public void onContent(String content) {
                receivedContent.append(content);
            }

            @Override
            public void onComplete(GenerateResult result) {
                assertNotNull(result);
            }

            @Override
            public void onError(String error) {
                fail("不应该出错: " + error);
            }
        });

        // Then
        assertTrue(receivedContent.length() > 0);
    }

    @Test
    void testStreamGenerationWithEmptyPrompt() {
        // Given
        final boolean[] errorCalled = {false};

        // When
        generationService.generateStream("", "context", 1000, 0.8, new GenerationService.StreamListener() {
            @Override
            public void onContent(String content) {
            }

            @Override
            public void onComplete(GenerateResult result) {
            }

            @Override
            public void onError(String error) {
                errorCalled[0] = true;
            }
        });

        // Then
        assertTrue(errorCalled[0]);
    }

    /**
     * 模拟 GenerationService 实现
     */
    private static class MockGenerationService implements GenerationService {

        @Override
        public GenerateResult generate(String prompt, String context, int maxLength, double temperature) {
            if (prompt == null || prompt.isEmpty()) {
                throw new IllegalArgumentException("提示词不能为空");
            }

            // 返回模拟结果
            String mockContent = "这是模拟生成的文本内容。".repeat(maxLength / 20);
            if (mockContent.length() > maxLength) {
                mockContent = mockContent.substring(0, maxLength);
            }

            return GenerateResult.builder()
                    .content(mockContent)
                    .wordCount(mockContent.length())
                    .model("qwen-max-longcontext")
                    .promptTokens(100)
                    .completionTokens(maxLength)
                    .totalTokens(100 + maxLength)
                    .build();
        }

        @Override
        public void generateStream(String prompt, String context, int maxLength, double temperature,
                                   StreamListener listener) {
            if (prompt == null || prompt.isEmpty()) {
                listener.onError("提示词不能为空");
                return;
            }

            // 模拟流式输出
            String content = "模拟流式生成的文本内容。";
            for (char c : content.toCharArray()) {
                listener.onContent(String.valueOf(c));
            }

            GenerateResult result = GenerateResult.builder()
                    .content(content)
                    .wordCount(content.length())
                    .model("qwen-max-longcontext")
                    .build();

            listener.onComplete(result);
        }
    }
}
