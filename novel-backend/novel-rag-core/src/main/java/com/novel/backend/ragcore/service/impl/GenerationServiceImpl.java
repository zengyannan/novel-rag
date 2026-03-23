package com.novel.backend.ragcore.service.impl;

import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.ragcore.service.GenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文本生成服务实现
 * 使用阿里云百炼 LLM
 */
@Slf4j
@Service
public class GenerationServiceImpl implements GenerationService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
            你是一位专业的小说写作助手，擅长创作各种类型的小说内容。
            请根据用户提供的背景信息和创作要求，创作出连贯、生动、富有感染力的小说内容。
            注意：
            1. 保持与原作风格一致
            2. 情节发展要自然合理
            3. 人物性格要前后一致
            4. 文笔要优美流畅
            """;

    private static final String DEFAULT_MODEL = "qwen-max-longcontext";

    public GenerationServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
        log.info("Generation服务初始化完成");
    }

    @Override
    public GenerateResult generate(String prompt, String context, int maxLength, double temperature) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("提示词不能为空");
        }

        log.info("开始生成文本，maxLength: {}, temperature: {}", maxLength, temperature);

        try {
            // 构建完整的提示词
            String fullPrompt = buildFullPrompt(prompt, context);

            // 使用 ChatClient 生成
            ChatClient chatClient = chatClientBuilder.build();

            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(fullPrompt)
                    .call()
                    .content();

            GenerateResult result = GenerateResult.builder()
                    .content(content)
                    .wordCount(content != null ? content.length() : 0)
                    .model(DEFAULT_MODEL)
                    .build();

            log.info("文本生成完成，字数: {}", result.getWordCount());
            return result;

        } catch (Exception e) {
            log.error("文本生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateStream(String prompt, String context, int maxLength, double temperature,
                               StreamListener listener) {
        if (prompt == null || prompt.isEmpty()) {
            listener.onError("提示词不能为空");
            return;
        }

        log.info("开始流式生成文本");

        try {
            String fullPrompt = buildFullPrompt(prompt, context);

            ChatClient chatClient = chatClientBuilder.build();

            StringBuilder contentBuilder = new StringBuilder();

            chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(fullPrompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        contentBuilder.append(chunk);
                        listener.onContent(chunk);
                    })
                    .doOnComplete(() -> {
                        GenerateResult result = GenerateResult.builder()
                                .content(contentBuilder.toString())
                                .wordCount(contentBuilder.length())
                                .model(DEFAULT_MODEL)
                                .build();
                        listener.onComplete(result);
                    })
                    .doOnError(error -> listener.onError(error.getMessage()))
                    .subscribe();

        } catch (Exception e) {
            log.error("流式生成失败: {}", e.getMessage(), e);
            listener.onError("流式生成失败: " + e.getMessage());
        }
    }

    /**
     * 构建完整的提示词
     */
    private String buildFullPrompt(String prompt, String context) {
        StringBuilder sb = new StringBuilder();

        if (context != null && !context.isEmpty()) {
            sb.append("【参考背景】\n");
            sb.append(context);
            sb.append("\n\n");
        }

        sb.append("【创作要求】\n");
        sb.append(prompt);

        return sb.toString();
    }
}
