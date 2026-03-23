package com.novel.backend.ragcore.service.impl;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;
import com.novel.backend.ragcore.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 生成流程服务实现
 */
@Slf4j
@Service
public class RagPipelineImpl implements RagPipeline {

    private final DocumentParser documentParser;
    private final TextSplitter textSplitter;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final GenerationService generationService;

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;
    private static final int DEFAULT_TOP_K = 5;

    public RagPipelineImpl(DocumentParser documentParser,
                           TextSplitter textSplitter,
                           EmbeddingService embeddingService,
                           VectorStoreService vectorStoreService,
                           GenerationService generationService) {
        this.documentParser = documentParser;
        this.textSplitter = textSplitter;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.generationService = generationService;
        log.info("RAG Pipeline 初始化完成");
    }

    @Override
    public GenerateResult execute(String documentContent, GenerateRequest request) {
        log.info("开始执行 RAG 生成流程");

        // 1. 解析文档
        log.info("步骤1: 解析文档");
        // 假设文档是文本格式

        // 2. 文本分块
        log.info("步骤2: 文本分块");
        List<TextChunk> chunks = textSplitter.splitText(documentContent, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);

        // 3. 向量化
        log.info("步骤3: 向量化，共 {} 个分块", chunks.size());
        List<String> contents = chunks.stream()
                .map(TextChunk::getContent)
                .collect(Collectors.toList());
        List<float[]> embeddings = embeddingService.embedBatch(contents);

        // 4. 存储向量
        log.info("步骤4: 存储向量");
        vectorStoreService.storeBatch(chunks, embeddings);

        // 5. 检索相关内容
        log.info("步骤5: 检索相关内容");
        List<TextChunk> relevantChunks = vectorStoreService.search(request.getPrompt(), DEFAULT_TOP_K);

        // 6. 构建上下文
        log.info("步骤6: 构建上下文");
        String context = buildContext(relevantChunks);

        // 7. 生成文本
        log.info("步骤7: 生成文本");
        GenerateResult result = generationService.generate(
                request.getPrompt(),
                context,
                request.getMaxLength(),
                request.getTemperature()
        );

        if (request.getChapterTitle() != null) {
            result.setChapterTitle(request.getChapterTitle());
        }

        log.info("RAG 生成流程完成");
        return result;
    }

    @Override
    public GenerateResult executeWithNovel(Long novelId, GenerateRequest request) {
        log.info("开始执行 RAG 生成流程，novelId: {}", novelId);

        // 检索相关内容
        List<TextChunk> relevantChunks = retrieveRelevantContent(novelId, request.getPrompt(), DEFAULT_TOP_K);

        // 构建上下文
        String context = buildContext(relevantChunks);

        // 生成文本
        GenerateResult result = generationService.generate(
                request.getPrompt(),
                context,
                request.getMaxLength(),
                request.getTemperature()
        );

        if (request.getChapterTitle() != null) {
            result.setChapterTitle(request.getChapterTitle());
        }

        log.info("RAG 生成流程完成");
        return result;
    }

    @Override
    public int indexDocument(Long novelId, ParsedDocument document) {
        log.info("开始索引文档，novelId: {}", novelId);

        // 1. 分块
        List<TextChunk> chunks = textSplitter.split(document, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);

        // 2. 向量化
        List<String> contents = chunks.stream()
                .map(TextChunk::getContent)
                .collect(Collectors.toList());
        List<float[]> embeddings = embeddingService.embedBatch(contents);

        // 3. 存储
        vectorStoreService.storeBatch(chunks, embeddings);

        log.info("文档索引完成，共 {} 个分块", chunks.size());
        return chunks.size();
    }

    @Override
    public List<TextChunk> retrieveRelevantContent(Long novelId, String query, int topK) {
        log.info("检索相关内容，novelId: {}, query: {}, topK: {}", novelId, query, topK);
        return vectorStoreService.search(query, topK);
    }

    @Override
    public String buildContext(List<TextChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        return chunks.stream()
                .map(chunk -> {
                    String content = chunk.getContent();
                    if (chunk.getSourceChapter() != null) {
                        return "【第" + chunk.getSourceChapter() + "章相关内容】\n" + content;
                    }
                    return content;
                })
                .collect(Collectors.joining("\n\n"));
    }
}
