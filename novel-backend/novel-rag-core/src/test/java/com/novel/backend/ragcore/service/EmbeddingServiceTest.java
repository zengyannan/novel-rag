package com.novel.backend.ragcore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmbeddingService 单元测试
 * 使用模拟实现测试
 */
class EmbeddingServiceTest {

    private MockEmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new MockEmbeddingService();
    }

    @Test
    void testGetDimension() {
        // When
        int dimension = embeddingService.getDimension();

        // Then
        assertEquals(1024, dimension);
    }

    @Test
    void testCosineSimilarityIdenticalVectors() {
        // Given
        float[] vector = {0.5f, 0.5f, 0.5f, 0.5f};

        // When
        double similarity = embeddingService.cosineSimilarity(vector, vector);

        // Then
        assertEquals(1.0, similarity, 0.0001);
    }

    @Test
    void testCosineSimilarityOrthogonalVectors() {
        // Given
        float[] vector1 = {1.0f, 0.0f, 0.0f, 0.0f};
        float[] vector2 = {0.0f, 1.0f, 0.0f, 0.0f};

        // When
        double similarity = embeddingService.cosineSimilarity(vector1, vector2);

        // Then
        assertEquals(0.0, similarity, 0.0001);
    }

    @Test
    void testCosineSimilarityOppositeVectors() {
        // Given
        float[] vector1 = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] vector2 = {-1.0f, -1.0f, -1.0f, -1.0f};

        // When
        double similarity = embeddingService.cosineSimilarity(vector1, vector2);

        // Then
        assertEquals(-1.0, similarity, 0.0001);
    }

    @Test
    void testCosineSimilarityWithNullVector1() {
        // Given
        float[] vector2 = {1.0f, 0.0f};

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingService.cosineSimilarity(null, vector2);
        });
    }

    @Test
    void testCosineSimilarityWithNullVector2() {
        // Given
        float[] vector1 = {1.0f, 0.0f};

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingService.cosineSimilarity(vector1, null);
        });
    }

    @Test
    void testCosineSimilarityWithDifferentDimensions() {
        // Given
        float[] vector1 = {1.0f, 0.0f};
        float[] vector2 = {1.0f, 0.0f, 0.0f};

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingService.cosineSimilarity(vector1, vector2);
        });
    }

    @Test
    void testCosineSimilarityWithZeroVector() {
        // Given
        float[] vector1 = {0.0f, 0.0f, 0.0f};
        float[] vector2 = {1.0f, 0.0f, 0.0f};

        // When
        double similarity = embeddingService.cosineSimilarity(vector1, vector2);

        // Then
        assertEquals(0.0, similarity, 0.0001);
    }

    @Test
    void testEmbedEmptyText() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingService.embed("");
        });
    }

    @Test
    void testEmbedNullText() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingService.embed(null);
        });
    }

    @Test
    void testEmbedBatchWithEmptyList() {
        // When
        var result = embeddingService.embedBatch(java.util.List.of());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testEmbedBatchWithNullList() {
        // When
        var result = embeddingService.embedBatch(null);

        // Then
        assertTrue(result.isEmpty());
    }

    /**
     * 模拟 EmbeddingService 实现
     */
    private static class MockEmbeddingService implements EmbeddingService {

        @Override
        public float[] embed(String text) {
            if (text == null || text.isEmpty()) {
                throw new IllegalArgumentException("文本内容不能为空");
            }
            // 返回模拟向量
            float[] embedding = new float[1024];
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] = 0.1f;
            }
            return embedding;
        }

        @Override
        public java.util.List<float[]> embedBatch(java.util.List<String> texts) {
            if (texts == null || texts.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            java.util.List<float[]> results = new java.util.ArrayList<>();
            for (String text : texts) {
                results.add(embed(text));
            }
            return results;
        }

        @Override
        public int getDimension() {
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
}
