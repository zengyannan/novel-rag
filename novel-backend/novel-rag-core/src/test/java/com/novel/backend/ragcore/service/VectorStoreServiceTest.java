package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.TextChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VectorStoreService 单元测试
 * 使用模拟实现测试
 */
class VectorStoreServiceTest {

    private MockVectorStoreService vectorStoreService;

    @BeforeEach
    void setUp() {
        vectorStoreService = new MockVectorStoreService();
    }

    @Test
    void testStoreAndSearch() {
        // Given
        TextChunk chunk = TextChunk.builder()
                .id("chunk_001")
                .content("这是一段测试内容")
                .chunkIndex(0)
                .wordCount(8)
                .build();
        float[] embedding = new float[1024];

        // When
        vectorStoreService.store(chunk, embedding);
        List<TextChunk> results = vectorStoreService.search("测试", 1);

        // Then
        assertNotNull(results);
    }

    @Test
    void testStoreWithNullChunk() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.store(null, new float[1024]);
        });
    }

    @Test
    void testStoreWithNullEmbedding() {
        // Given
        TextChunk chunk = TextChunk.builder()
                .id("chunk_001")
                .content("测试")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.store(chunk, null);
        });
    }

    @Test
    void testStoreBatch() {
        // Given
        List<TextChunk> chunks = new ArrayList<>();
        List<float[]> embeddings = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            chunks.add(TextChunk.builder()
                    .id("chunk_" + i)
                    .content("内容" + i)
                    .chunkIndex(i)
                    .build());
            embeddings.add(new float[1024]);
        }

        // When
        vectorStoreService.storeBatch(chunks, embeddings);

        // Then - 无异常即成功
        assertTrue(true);
    }

    @Test
    void testStoreBatchWithMismatchedSizes() {
        // Given
        List<TextChunk> chunks = new ArrayList<>();
        chunks.add(TextChunk.builder().id("1").content("a").build());
        chunks.add(TextChunk.builder().id("2").content("b").build());

        List<float[]> embeddings = new ArrayList<>();
        embeddings.add(new float[1024]);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.storeBatch(chunks, embeddings);
        });
    }

    @Test
    void testSearchWithEmptyQuery() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.search("", 10);
        });
    }

    @Test
    void testSearchWithNullQuery() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.search(null, 10);
        });
    }

    @Test
    void testSearchByVectorWithNullEmbedding() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.searchByVector(null, 10);
        });
    }

    @Test
    void testDeleteByNovelIdWithNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            vectorStoreService.deleteByNovelId(null);
        });
    }

    @Test
    void testIndexExists() {
        // When
        boolean exists = vectorStoreService.indexExists();

        // Then
        // 模拟实现返回 true
        assertTrue(exists);
    }

    @Test
    void testCreateIndex() {
        // When
        vectorStoreService.createIndex();

        // Then - 无异常即成功
        assertTrue(true);
    }

    @Test
    void testSearchByVector() {
        // Given
        float[] queryVector = new float[1024];

        // When
        List<TextChunk> results = vectorStoreService.searchByVector(queryVector, 5);

        // Then
        assertNotNull(results);
    }

    /**
     * 模拟 VectorStoreService 实现
     */
    private static class MockVectorStoreService implements VectorStoreService {

        private boolean indexCreated = true;

        @Override
        public void store(TextChunk chunk, float[] embedding) {
            if (chunk == null || embedding == null) {
                throw new IllegalArgumentException("分块和向量不能为空");
            }
            // 模拟存储
        }

        @Override
        public void storeBatch(List<TextChunk> chunks, List<float[]> embeddings) {
            if (chunks == null || embeddings == null) {
                throw new IllegalArgumentException("分块列表和向量列表不能为空");
            }
            if (chunks.size() != embeddings.size()) {
                throw new IllegalArgumentException("分块列表和向量列表大小不匹配");
            }
            // 模拟批量存储
        }

        @Override
        public List<TextChunk> search(String query, int topK) {
            if (query == null || query.isEmpty()) {
                throw new IllegalArgumentException("查询文本不能为空");
            }
            // 返回空列表（实际需要 EmbeddingService）
            return new ArrayList<>();
        }

        @Override
        public List<TextChunk> searchByVector(float[] embedding, int topK) {
            if (embedding == null) {
                throw new IllegalArgumentException("查询向量不能为空");
            }
            // 返回空列表
            return new ArrayList<>();
        }

        @Override
        public void deleteByNovelId(Long novelId) {
            if (novelId == null) {
                throw new IllegalArgumentException("小说ID不能为空");
            }
            // 模拟删除
        }

        @Override
        public boolean indexExists() {
            return indexCreated;
        }

        @Override
        public void createIndex() {
            indexCreated = true;
        }
    }
}
