package com.novel.backend.ragcore.service.impl;

import com.novel.backend.ragcore.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本向量化服务实现
 * 使用阿里云百炼 text-embedding-v3 模型
 */
@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingServiceImpl(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        log.info("Embedding服务初始化完成");
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }

        log.debug("开始向量化文本，长度: {}", text.length());

        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            if (response == null || response.getResults().isEmpty()) {
                throw new RuntimeException("向量化失败：响应为空");
            }

            float[] embedding = response.getResults().get(0).getOutput();
            log.debug("向量化完成，维度: {}", embedding.length);
            return embedding;
        } catch (Exception e) {
            log.error("向量化文本失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("开始批量向量化，文本数量: {}", texts.size());

        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(texts);
            if (response == null) {
                throw new RuntimeException("批量向量化失败：响应为空");
            }

            List<float[]> embeddings = new ArrayList<>();
            response.getResults().forEach(result -> {
                embeddings.add(result.getOutput());
            });

            log.debug("批量向量化完成，数量: {}", embeddings.size());
            return embeddings;
        } catch (Exception e) {
            log.error("批量向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量向量化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getDimension() {
        // text-embedding-v3 模型的向量维度为 1024
        return 1024;
    }

    @Override
    public double cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("向量不能为空");
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
