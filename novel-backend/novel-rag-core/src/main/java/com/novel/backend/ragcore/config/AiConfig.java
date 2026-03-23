package com.novel.backend.ragcore.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 配置类
 * 提供模拟的 AI 服务 Bean（用于开发和测试环境）
 */
@Configuration
public class AiConfig {

    private static final int EMBEDDING_DIMENSION = 1024;

    /**
     * 模拟 EmbeddingModel
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingModel embeddingModel() {
        return new MockEmbeddingModel();
    }

    /**
     * 模拟 ChatModel
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatModel chatModel() {
        return new MockChatModel();
    }

    /**
     * ChatClient Builder
     */
    @Bean
    @ConditionalOnMissingBean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * 创建模拟向量
     */
    private static float[] createMockEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSION];
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            embedding[i] = (float) (Math.random() * 2 - 1);
        }
        return embedding;
    }

    /**
     * 模拟 EmbeddingModel 实现
     */
    private static class MockEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> texts = request.getInstructions();
            List<Embedding> embeddings = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                embeddings.add(new Embedding(createMockEmbedding(), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return createMockEmbedding();
        }

        @Override
        public float[] embed(String text) {
            return createMockEmbedding();
        }

        @Override
        public int dimensions() {
            return EMBEDDING_DIMENSION;
        }
    }

    /**
     * 模拟 ChatModel 实现
     */
    private static class MockChatModel implements ChatModel {

        @Override
        public ChatResponse call(Prompt prompt) {
            String mockContent = "这是模拟生成的文本内容。在实际使用中，请配置阿里云百炼 API Key 以获得真实的 AI 生成能力。";
            AssistantMessage message = new AssistantMessage(mockContent);
            return new ChatResponse(List.of(new Generation(message)));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }
}
