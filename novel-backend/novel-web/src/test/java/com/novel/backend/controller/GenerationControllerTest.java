package com.novel.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.service.GenerationApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GenerationController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class GenerationControllerTest {

    @Mock
    private GenerationApiService generationApiService;

    @InjectMocks
    private GenerationController generationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(generationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGenerateChapter() throws Exception {
        // Given
        GenerateRequest request = GenerateRequest.builder()
                .novelId(1L)
                .prompt("写一段主角战斗的场景")
                .chapterTitle("决战")
                .maxLength(2000)
                .temperature(0.8)
                .build();

        GenerateResult result = GenerateResult.builder()
                .content("生成的小说内容...")
                .chapterTitle("决战")
                .wordCount(100)
                .model("qwen-max-longcontext")
                .build();

        when(generationApiService.generateChapter(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/generate/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.wordCount").value(100));

        verify(generationApiService, times(1)).generateChapter(any());
    }

    @Test
    void testGenerateChapterWithDefaultValues() throws Exception {
        // Given
        GenerateRequest request = GenerateRequest.builder()
                .novelId(1L)
                .prompt("测试提示词")
                .build();

        GenerateResult result = GenerateResult.builder()
                .content("生成的内容")
                .wordCount(50)
                .build();

        when(generationApiService.generateChapter(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/generate/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGenerateChapterWithInvalidNovelId() throws Exception {
        // Given
        GenerateRequest request = GenerateRequest.builder()
                .prompt("测试")
                .build(); // novelId is null

        // When & Then
        mockMvc.perform(post("/api/generate/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateChapterWithEmptyPrompt() throws Exception {
        // Given
        GenerateRequest request = GenerateRequest.builder()
                .novelId(1L)
                .prompt("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/generate/chapter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateChapterStreamEndpoint() throws Exception {
        // Given
        // 模拟流式回调
        doAnswer(invocation -> {
            GenerationApiService.StreamCallback callback = invocation.getArgument(1);
            callback.onContent("测试内容");
            callback.onComplete(GenerateResult.builder()
                    .content("测试内容")
                    .wordCount(4)
                    .build());
            return null;
        }).when(generationApiService).generateChapterStream(any(), any());

        // When & Then
        mockMvc.perform(get("/api/generate/chapter/stream")
                        .param("novelId", "1")
                        .param("prompt", "测试"))
                .andExpect(status().isOk());

        verify(generationApiService, times(1)).generateChapterStream(any(), any());
    }
}
