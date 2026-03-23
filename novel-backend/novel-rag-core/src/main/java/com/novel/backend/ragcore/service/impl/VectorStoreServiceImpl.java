package com.novel.backend.ragcore.service.impl;

import com.novel.backend.ragcore.model.TextChunk;
import com.novel.backend.ragcore.service.EmbeddingService;
import com.novel.backend.ragcore.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 向量存储服务实现
 * 使用内存存储（简化实现，实际生产应使用 Redis 向量索引）
 */
@Slf4j
@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final String INDEX_NAME = "novel_vectors";
    private static final String KEY_PREFIX = "novel:chunk:";
    private static final int VECTOR_DIMENSION = 1024;

    private final EmbeddingService embeddingService;

    // Redis 模板（可选，用于生产环境）
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // 内存存储（用于测试环境）
    private final Map<String, TextChunk> chunkStore = new HashMap<>();
    private final Map<String, float[]> embeddingStore = new HashMap<>();

    public VectorStoreServiceImpl(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        log.info("VectorStore服务初始化完成，使用内存存储模式");
    }

    @Override
    public void store(TextChunk chunk, float[] embedding) {
        if (chunk == null || embedding == null) {
            throw new IllegalArgumentException("分块和向量不能为空");
        }

        String key = KEY_PREFIX + chunk.getId();

        try {
            // 存储到内存
            chunkStore.put(key, chunk);
            embeddingStore.put(key, embedding);

            log.debug("存储向量成功: {}", key);
        } catch (Exception e) {
            log.error("存储向量失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储向量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void storeBatch(List<TextChunk> chunks, List<float[]> embeddings) {
        if (chunks == null || embeddings == null) {
            throw new IllegalArgumentException("分块列表和向量列表不能为空");
        }
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("分块列表和向量列表大小不匹配");
        }

        log.info("开始批量存储向量，数量: {}", chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            store(chunks.get(i), embeddings.get(i));
        }

        log.info("批量存储向量完成");
    }

    @Override
    public List<TextChunk> search(String query, int topK) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("查询文本不能为空");
        }

        log.debug("开始搜索，查询: {}, topK: {}", query, topK);

        float[] queryEmbedding = embeddingService.embed(query);
        return searchByVector(queryEmbedding, topK);
    }

    @Override
    public List<TextChunk> searchByVector(float[] embedding, int topK) {
        if (embedding == null) {
            throw new IllegalArgumentException("查询向量不能为空");
        }

        List<TextChunk> results = new ArrayList<>();

        try {
            // 计算所有存储的向量与查询向量的相似度
            List<Map.Entry<String, Double>> similarities = new ArrayList<>();

            for (Map.Entry<String, float[]> entry : embeddingStore.entrySet()) {
                double similarity = embeddingService.cosineSimilarity(embedding, entry.getValue());
                similarities.add(new AbstractMap.SimpleEntry<>(entry.getKey(), similarity));
            }

            // 按相似度降序排序
            similarities.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            // 取前 topK 个结果
            int count = 0;
            for (Map.Entry<String, Double> entry : similarities) {
                if (count >= topK) break;
                TextChunk chunk = chunkStore.get(entry.getKey());
                if (chunk != null) {
                    results.add(chunk);
                    count++;
                }
            }

            log.debug("搜索完成，找到 {} 个结果", results.size());
        } catch (Exception e) {
            log.warn("向量搜索失败: {}，返回空结果", e.getMessage());
        }

        return results;
    }

    @Override
    public void deleteByNovelId(Long novelId) {
        if (novelId == null) {
            throw new IllegalArgumentException("小说ID不能为空");
        }

        log.info("开始删除小说相关向量，novelId: {}", novelId);

        try {
            // 清除所有存储
            chunkStore.clear();
            embeddingStore.clear();
            log.info("删除向量完成");
        } catch (Exception e) {
            log.error("删除向量失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除向量失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean indexExists() {
        return true;
    }

    @Override
    public void createIndex() {
        log.info("向量索引已就绪");
    }
}
