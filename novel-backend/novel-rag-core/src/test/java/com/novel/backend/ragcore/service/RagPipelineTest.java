package com.novel.backend.ragcore.service;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RagPipeline 单元测试
 * 使用模拟实现测试
 */
class RagPipelineTest {

    private MockRagPipeline ragPipeline;

    @BeforeEach
    void setUp() {
        ragPipeline = new MockRagPipeline();
    }

    @Test
    void testExecuteWithValidInput() {
        // Given
        String documentContent = "这是测试文档内容。主角是一位勇敢的剑客。";
        GenerateRequest request = GenerateRequest.builder()
                .novelId(1L)
                .prompt("写一段主角战斗的场景")
                .maxLength(1000)
                .temperature(0.8)
                .build();

        // When
        GenerateResult result = ragPipeline.execute(documentContent, request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getWordCount() > 0);
    }

    @Test
    void testExecuteWithNovel() {
        // Given
        Long novelId = 1L;
        GenerateRequest request = GenerateRequest.builder()
                .novelId(novelId)
                .prompt("测试生成")
                .maxLength(500)
                .build();

        // When
        GenerateResult result = ragPipeline.executeWithNovel(novelId, request);

        // Then
        assertNotNull(result);
    }

    @Test
    void testIndexDocument() {
        // Given
        Long novelId = 1L;
        ParsedDocument document = ParsedDocument.builder()
                .content("测试内容")
                .wordCount(4)
                .build();

        // When
        int count = ragPipeline.indexDocument(novelId, document);

        // Then
        assertTrue(count > 0);
    }

    @Test
    void testRetrieveRelevantContent() {
        // Given
        Long novelId = 1L;
        String query = "战斗场景";
        int topK = 5;

        // When
        List<TextChunk> chunks = ragPipeline.retrieveRelevantContent(novelId, query, topK);

        // Then
        assertNotNull(chunks);
    }

    @Test
    void testBuildContextWithChunks() {
        // Given
        List<TextChunk> chunks = new ArrayList<>();
        chunks.add(TextChunk.builder()
                .content("第一段相关内容")
                .sourceChapter(1)
                .build());
        chunks.add(TextChunk.builder()
                .content("第二段相关内容")
                .sourceChapter(2)
                .build());

        // When
        String context = ragPipeline.buildContext(chunks);

        // Then
        assertNotNull(context);
        assertTrue(context.contains("第一段相关内容"));
        assertTrue(context.contains("第二段相关内容"));
        assertTrue(context.contains("第1章"));
        assertTrue(context.contains("第2章"));
    }

    @Test
    void testBuildContextWithEmptyChunks() {
        // Given
        List<TextChunk> chunks = new ArrayList<>();

        // When
        String context = ragPipeline.buildContext(chunks);

        // Then
        assertEquals("", context);
    }

    @Test
    void testBuildContextWithNullChunks() {
        // When
        String context = ragPipeline.buildContext(null);

        // Then
        assertEquals("", context);
    }

    @Test
    void testBuildContextWithNoSourceChapter() {
        // Given
        List<TextChunk> chunks = new ArrayList<>();
        chunks.add(TextChunk.builder()
                .content("没有章节信息的内容")
                .build());

        // When
        String context = ragPipeline.buildContext(chunks);

        // Then
        assertNotNull(context);
        assertTrue(context.contains("没有章节信息的内容"));
    }

    /**
     * 模拟 RagPipeline 实现
     */
    private static class MockRagPipeline implements RagPipeline {

        @Override
        public GenerateResult execute(String documentContent, GenerateRequest request) {
            // 模拟 RAG 流程
            return GenerateResult.builder()
                    .content("模拟生成的内容：根据文档内容生成的文本。")
                    .wordCount(100)
                    .model("qwen-max-longcontext")
                    .build();
        }

        @Override
        public GenerateResult executeWithNovel(Long novelId, GenerateRequest request) {
            return GenerateResult.builder()
                    .content("模拟基于小说生成的内容。")
                    .wordCount(50)
                    .model("qwen-max-longcontext")
                    .build();
        }

        @Override
        public int indexDocument(Long novelId, ParsedDocument document) {
            // 模拟索引
            return document != null ? 10 : 0;
        }

        @Override
        public List<TextChunk> retrieveRelevantContent(Long novelId, String query, int topK) {
            List<TextChunk> chunks = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, 3); i++) {
                chunks.add(TextChunk.builder()
                        .id("chunk_" + i)
                        .content("相关内容 " + i)
                        .chunkIndex(i)
                        .sourceChapter(i + 1)
                        .build());
            }
            return chunks;
        }

        @Override
        public String buildContext(List<TextChunk> chunks) {
            if (chunks == null || chunks.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (TextChunk chunk : chunks) {
                if (chunk.getSourceChapter() != null) {
                    sb.append("【第").append(chunk.getSourceChapter()).append("章相关内容】\n");
                }
                sb.append(chunk.getContent()).append("\n\n");
            }
            return sb.toString().trim();
        }
    }
}
