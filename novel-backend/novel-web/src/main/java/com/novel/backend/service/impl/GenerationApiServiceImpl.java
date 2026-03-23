package com.novel.backend.service.impl;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.ragcore.service.GenerationService;
import com.novel.backend.ragcore.service.RagPipeline;
import com.novel.backend.service.GenerationApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 生成 API 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationApiServiceImpl implements GenerationApiService {

    private final RagPipeline ragPipeline;
    private final GenerationService generationService;

    @Override
    public GenerateResult generateChapter(GenerateRequest request) {
        log.info("开始生成章节，novelId: {}, prompt: {}", request.getNovelId(), request.getPrompt());

        // 使用 RAG 流程生成
        GenerateResult result = ragPipeline.executeWithNovel(request.getNovelId(), request);

        if (request.getChapterTitle() != null) {
            result.setChapterTitle(request.getChapterTitle());
        }

        log.info("章节生成完成，字数: {}", result.getWordCount());
        return result;
    }

    @Override
    public void generateChapterStream(GenerateRequest request, StreamCallback callback) {
        log.info("开始流式生成章节，novelId: {}", request.getNovelId());

        // 获取上下文
        var chunks = ragPipeline.retrieveRelevantContent(
                request.getNovelId(),
                request.getPrompt(),
                5
        );

        String context = ragPipeline.buildContext(chunks);

        // 使用流式生成
        generationService.generateStream(
                request.getPrompt(),
                context,
                request.getMaxLength(),
                request.getTemperature(),
                new GenerationService.StreamListener() {
                    @Override
                    public void onContent(String content) {
                        callback.onContent(content);
                    }

                    @Override
                    public void onComplete(GenerateResult result) {
                        if (request.getChapterTitle() != null) {
                            result.setChapterTitle(request.getChapterTitle());
                        }
                        callback.onComplete(result);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}
