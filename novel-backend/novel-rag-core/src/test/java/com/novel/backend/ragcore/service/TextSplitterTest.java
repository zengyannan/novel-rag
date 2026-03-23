package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;
import com.novel.backend.ragcore.service.impl.TextSplitterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TextSplitter 单元测试
 */
class TextSplitterTest {

    private TextSplitter textSplitter;

    @BeforeEach
    void setUp() {
        textSplitter = new TextSplitterImpl();
    }

    @Test
    void testSplitTextBasic() {
        // Given
        String text = "这是第一段内容。这是一些测试文字。" +
                "\n\n这是第二段内容。继续测试。" +
                "\n\n这是第三段内容。测试分块功能。";

        // When
        List<TextChunk> chunks = textSplitter.splitText(text, 100, 10);

        // Then
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());

        // 验证每个chunk都有内容
        for (TextChunk chunk : chunks) {
            assertNotNull(chunk.getId());
            assertNotNull(chunk.getContent());
            assertFalse(chunk.getContent().isEmpty());
            assertNotNull(chunk.getChunkIndex());
            assertTrue(chunk.getWordCount() > 0);
        }
    }

    @Test
    void testSplitTextWithDefaultParameters() {
        // Given
        ParsedDocument document = ParsedDocument.builder()
                .content("测试内容")
                .build();

        // When
        List<TextChunk> chunks = textSplitter.split(document);

        // Then
        assertNotNull(chunks);
    }

    @Test
    void testSplitDocumentWithChapters() {
        // Given
        ParsedDocument.Chapter chapter1 = ParsedDocument.Chapter.builder()
                .chapterNumber(1)
                .title("第一章")
                .content("这是第一章的内容。这一章讲述了主角的出生和成长经历。" +
                        "在一个风雨交加的夜晚，主角来到了这个世界。")
                .build();

        ParsedDocument.Chapter chapter2 = ParsedDocument.Chapter.builder()
                .chapterNumber(2)
                .title("第二章")
                .content("这是第二章的内容。主角开始了他的冒险旅程。" +
                        "他遇到了许多有趣的人物和事件。")
                .build();

        ParsedDocument document = ParsedDocument.builder()
                .content("完整内容")
                .chapters(Arrays.asList(chapter1, chapter2))
                .build();

        // When
        List<TextChunk> chunks = textSplitter.split(document, 100, 10);

        // Then
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());

        // 验证chunk有来源章节信息
        for (TextChunk chunk : chunks) {
            assertNotNull(chunk.getSourceChapter());
        }
    }

    @Test
    void testSplitEmptyText() {
        // When
        List<TextChunk> chunks = textSplitter.splitText("", 100, 10);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitNullText() {
        // When
        List<TextChunk> chunks = textSplitter.splitText(null, 100, 10);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitNullDocument() {
        // When
        List<TextChunk> chunks = textSplitter.split(null, 100, 10);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitDocumentWithNullContent() {
        // Given
        ParsedDocument document = ParsedDocument.builder()
                .content(null)
                .build();

        // When
        List<TextChunk> chunks = textSplitter.split(document, 100, 10);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSplitTextWithInvalidChunkSize() {
        // Given
        String text = "测试内容";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            textSplitter.splitText(text, 0, 10);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            textSplitter.splitText(text, -1, 10);
        });
    }

    @Test
    void testSplitTextWithInvalidOverlap() {
        // Given
        String text = "测试内容";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            textSplitter.splitText(text, 100, -1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            textSplitter.splitText(text, 100, 100);
        });
    }

    @Test
    void testSplitLongText() {
        // Given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("这是第").append(i).append("段内容，用于测试长文本的分块效果。");
            sb.append("\n\n");
        }
        String longText = sb.toString();

        // When
        List<TextChunk> chunks = textSplitter.splitText(longText, 500, 50);

        // Then
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());

        // 验证chunk内容连续性
        StringBuilder reconstructed = new StringBuilder();
        for (TextChunk chunk : chunks) {
            reconstructed.append(chunk.getContent());
        }
        // 检查大部分内容是否保留
        assertTrue(reconstructed.toString().contains("这是第0段内容"));
        assertTrue(reconstructed.toString().contains("这是第99段内容"));
    }

    @Test
    void testSplitChapter() {
        // Given
        ParsedDocument.Chapter chapter = ParsedDocument.Chapter.builder()
                .chapterNumber(5)
                .title("第五章")
                .content("这是第五章的测试内容。这一章有足够多的文字来测试分块功能。" +
                        "分块应该按照指定的大小进行，同时保持内容的完整性。")
                .build();

        // When
        List<TextChunk> chunks = textSplitter.splitChapter(chapter, 100, 10);

        // Then
        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());

        for (TextChunk chunk : chunks) {
            assertEquals(5, chunk.getSourceChapter());
        }
    }

    @Test
    void testChunkIdGeneration() {
        // Given
        String text = "测试内容用于验证ID生成。";

        // When
        List<TextChunk> chunks = textSplitter.splitText(text, 100, 10);

        // Then
        for (TextChunk chunk : chunks) {
            assertNotNull(chunk.getId());
            assertFalse(chunk.getId().isEmpty());
            // UUID格式验证
            assertTrue(chunk.getId().matches("[0-9a-f-]{36}"));
        }
    }

    @Test
    void testChunkWordCount() {
        // Given
        String text = "测试字数统计功能。";

        // When
        List<TextChunk> chunks = textSplitter.splitText(text, 100, 10);

        // Then
        for (TextChunk chunk : chunks) {
            assertEquals(chunk.getContent().length(), chunk.getWordCount());
        }
    }
}
