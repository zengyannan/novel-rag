package com.novel.backend.controller;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.common.vo.Result;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.service.GenerationApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 章节生成控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
@Tag(name = "章节生成", description = "基于RAG的小说章节生成接口")
public class GenerationController {

    private final GenerationApiService generationApiService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 生成章节
     */
    @PostMapping("/chapter")
    @Operation(summary = "生成章节", description = "根据提示词生成小说章节")
    public Result<GenerateResult> generateChapter(
            @Valid @RequestBody GenerateRequest request) {

        log.info("收到章节生成请求，novelId: {}", request.getNovelId());
        GenerateResult result = generationApiService.generateChapter(request);
        return Result.success(result);
    }

    /**
     * 流式生成章节
     */
    @GetMapping(value = "/chapter/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式生成章节", description = "实时返回生成的内容")
    public SseEmitter generateChapterStream(
            @Parameter(description = "小说ID") @RequestParam Long novelId,
            @Parameter(description = "提示词") @RequestParam String prompt,
            @Parameter(description = "章节标题") @RequestParam(required = false) String chapterTitle,
            @Parameter(description = "最大长度") @RequestParam(defaultValue = "2000") Integer maxLength,
            @Parameter(description = "温度参数") @RequestParam(defaultValue = "0.8") Double temperature) {

        log.info("收到流式生成请求，novelId: {}", novelId);

        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时

        executorService.execute(() -> {
            try {
                GenerateRequest request = GenerateRequest.builder()
                        .novelId(novelId)
                        .prompt(prompt)
                        .chapterTitle(chapterTitle)
                        .maxLength(maxLength)
                        .temperature(temperature)
                        .build();

                generationApiService.generateChapterStream(request, new GenerationApiService.StreamCallback() {
                    @Override
                    public void onContent(String content) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("content")
                                    .data(Map.of("text", content)));
                        } catch (Exception e) {
                            log.error("发送SSE消息失败", e);
                        }
                    }

                    @Override
                    public void onComplete(GenerateResult result) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data(Map.of(
                                            "wordCount", result.getWordCount(),
                                            "model", result.getModel()
                                    )));
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("发送完成消息失败", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("message", error)));
                        } catch (Exception e) {
                            log.error("发送错误消息失败", e);
                        }
                        emitter.completeWithError(new RuntimeException(error));
                    }
                });

            } catch (Exception e) {
                log.error("流式生成失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
