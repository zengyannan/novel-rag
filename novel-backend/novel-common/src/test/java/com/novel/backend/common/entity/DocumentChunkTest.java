package com.novel.backend.common.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocumentChunk 测试
 */
class DocumentChunkTest {

    @Test
    void testDocumentChunkBuilder() {
        // Given
        String id = "chunk_001";
        Long novelId = 1L;
        Long chapterId = 1L;
        String content = "这是测试内容";
        float[] embedding = {0.1f, 0.2f, 0.3f};

        // When
        DocumentChunk chunk = DocumentChunk.builder()
                .id(id)
                .novelId(novelId)
                .chapterId(chapterId)
                .chunkIndex(0)
                .content(content)
                .embedding(embedding)
                .build();

        // Then
        assertEquals(id, chunk.getId());
        assertEquals(novelId, chunk.getNovelId());
        assertEquals(chapterId, chunk.getChapterId());
        assertEquals(0, chunk.getChunkIndex());
        assertEquals(content, chunk.getContent());
        assertArrayEquals(embedding, chunk.getEmbedding());
    }

    @Test
    void testGetWordCount() {
        // Given
        DocumentChunk chunk = DocumentChunk.builder()
                .content("这是一段测试文本，用于测试字数统计功能。")
                .build();

        // When
        int wordCount = chunk.getWordCount();

        // Then
        assertEquals(20, wordCount); // 字符串实际长度
    }

    @Test
    void testGetWordCountWithNullContent() {
        // Given
        DocumentChunk chunk = new DocumentChunk();

        // When
        int wordCount = chunk.getWordCount();

        // Then
        assertEquals(0, wordCount);
    }

    @Test
    void testNoArgsConstructor() {
        // When
        DocumentChunk chunk = new DocumentChunk();

        // Then
        assertNull(chunk.getId());
        assertNull(chunk.getContent());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        float[] embedding = {0.1f, 0.2f};

        // When
        DocumentChunk chunk = new DocumentChunk(
                "chunk_001",
                1L,
                1L,
                0,
                "内容",
                embedding,
                now
        );

        // Then
        assertEquals("chunk_001", chunk.getId());
        assertEquals(1L, chunk.getNovelId());
        assertEquals(1L, chunk.getChapterId());
        assertEquals(0, chunk.getChunkIndex());
        assertEquals("内容", chunk.getContent());
        assertEquals(now, chunk.getCreatedAt());
    }
}
