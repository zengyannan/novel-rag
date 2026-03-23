package com.novel.backend.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenerateRequest 测试
 */
class GenerateRequestTest {

    @Test
    void testGenerateRequestBuilder() {
        // Given
        Long novelId = 1L;
        String prompt = "写一段主角战斗的场景";
        String chapterTitle = "决战";
        String style = "热血";

        // When
        GenerateRequest request = GenerateRequest.builder()
                .novelId(novelId)
                .prompt(prompt)
                .chapterTitle(chapterTitle)
                .style(style)
                .maxLength(3000)
                .temperature(0.9)
                .build();

        // Then
        assertEquals(novelId, request.getNovelId());
        assertEquals(prompt, request.getPrompt());
        assertEquals(chapterTitle, request.getChapterTitle());
        assertEquals(style, request.getStyle());
        assertEquals(3000, request.getMaxLength());
        assertEquals(0.9, request.getTemperature());
    }

    @Test
    void testDefaultValues() {
        // When
        GenerateRequest request = GenerateRequest.builder()
                .novelId(1L)
                .prompt("测试")
                .build();

        // Then
        assertEquals(2000, request.getMaxLength());
        assertEquals(0.8, request.getTemperature());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        GenerateRequest request = new GenerateRequest();

        // Then
        assertNull(request.getNovelId());
        assertNull(request.getPrompt());
    }

    @Test
    void testAllArgsConstructor() {
        // When
        GenerateRequest request = new GenerateRequest(
                1L,
                "测试提示词",
                "测试标题",
                "玄幻",
                2000,
                0.8
        );

        // Then
        assertEquals(1L, request.getNovelId());
        assertEquals("测试提示词", request.getPrompt());
        assertEquals("测试标题", request.getChapterTitle());
        assertEquals("玄幻", request.getStyle());
        assertEquals(2000, request.getMaxLength());
        assertEquals(0.8, request.getTemperature());
    }
}
