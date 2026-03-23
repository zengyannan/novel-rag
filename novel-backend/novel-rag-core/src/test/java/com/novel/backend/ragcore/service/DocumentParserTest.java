package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.service.impl.DocumentParserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocumentParser 单元测试
 */
class DocumentParserTest {

    private DocumentParser documentParser;

    @BeforeEach
    void setUp() {
        documentParser = new DocumentParserImpl();
    }

    @Test
    void testGetSupportedTypes() {
        // When
        String[] types = documentParser.getSupportedTypes();

        // Then
        assertEquals(4, types.length);
        assertArrayEquals(new String[]{"txt", "pdf", "docx", "epub"}, types);
    }

    @Test
    void testSupportsValidType() {
        assertTrue(documentParser.supports("txt"));
        assertTrue(documentParser.supports("TXT"));
        assertTrue(documentParser.supports("pdf"));
        assertTrue(documentParser.supports("docx"));
        assertTrue(documentParser.supports("epub"));
    }

    @Test
    void testSupportsInvalidType() {
        assertFalse(documentParser.supports("xyz"));
        assertFalse(documentParser.supports(null));
        assertFalse(documentParser.supports(""));
    }

    @Test
    void testParseTxtFile() {
        // Given
        String content = "第一章 开始\n\n这是第一章的内容。\n\n第二章 继续\n\n这是第二章的内容。";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        ParsedDocument document = documentParser.parse(inputStream, "txt");

        // Then
        assertNotNull(document);
        assertNotNull(document.getContent());
        assertTrue(document.getContent().contains("第一章"));
        assertTrue(document.getContent().contains("第二章"));
    }

    @Test
    void testParseTxtWithChapters() {
        // Given
        String content = "第一章 开始\n\n这是第一章的内容，讲述了一个精彩的故事。\n\n" +
                "第二章 冒险\n\n主角开始了他的冒险旅程。\n\n" +
                "第三章 结局\n\n故事迎来了结局。";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        ParsedDocument document = documentParser.parse(inputStream, "txt");

        // Then
        assertNotNull(document);
        List<ParsedDocument.Chapter> chapters = document.getChapters();
        assertNotNull(chapters);
        assertEquals(3, chapters.size());

        // 验证第一章
        ParsedDocument.Chapter chapter1 = chapters.get(0);
        assertEquals(1, chapter1.getChapterNumber());
        assertTrue(chapter1.getTitle().contains("第一章"));
        assertTrue(chapter1.getContent().contains("这是第一章的内容"));

        // 验证第二章
        ParsedDocument.Chapter chapter2 = chapters.get(1);
        assertEquals(2, chapter2.getChapterNumber());
        assertTrue(chapter2.getTitle().contains("第二章"));

        // 验证第三章
        ParsedDocument.Chapter chapter3 = chapters.get(2);
        assertEquals(3, chapter3.getChapterNumber());
        assertTrue(chapter3.getTitle().contains("第三章"));
    }

    @Test
    void testParseTxtWithoutChapters() {
        // Given
        String content = "这是一段没有章节标记的文本内容。它应该被当作一个整体章节处理。";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        ParsedDocument document = documentParser.parse(inputStream, "txt");

        // Then
        assertNotNull(document);
        List<ParsedDocument.Chapter> chapters = document.getChapters();
        assertNotNull(chapters);
        assertEquals(1, chapters.size());

        ParsedDocument.Chapter chapter = chapters.get(0);
        assertEquals(1, chapter.getChapterNumber());
        assertEquals("第一章", chapter.getTitle());
        assertTrue(chapter.getContent().contains("没有章节标记"));
    }

    @Test
    void testParseInvalidFileType() {
        // Given
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            documentParser.parse(inputStream, "invalid");
        });
    }

    @Test
    void testWordCountCalculation() {
        // Given
        String content = "测试内容";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        ParsedDocument document = documentParser.parse(inputStream, "txt");

        // Then
        assertEquals(4, document.getWordCount());
    }

    @Test
    void testParseEmptyContent() {
        // Given
        String content = "";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // When
        ParsedDocument document = documentParser.parse(inputStream, "txt");

        // Then
        assertNotNull(document);
        assertEquals("", document.getContent());
        assertEquals(0, document.getWordCount());
    }
}
