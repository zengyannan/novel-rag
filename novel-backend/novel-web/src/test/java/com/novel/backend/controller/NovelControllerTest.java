package com.novel.backend.controller;

import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.vo.Result;
import com.novel.backend.service.NovelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NovelController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class NovelControllerTest {

    @Mock
    private NovelService novelService;

    @InjectMocks
    private NovelController novelController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(novelController).build();
    }

    @Test
    void testGetNovelList() throws Exception {
        // Given
        Novel novel1 = Novel.builder()
                .id(1L)
                .name("小说1")
                .author("作者1")
                .build();
        Novel novel2 = Novel.builder()
                .id(2L)
                .name("小说2")
                .author("作者2")
                .build();

        Result<List<Novel>> result = Result.success(Arrays.asList(novel1, novel2));
        when(novelService.getNovelList(0, 10)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/novels")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetNovelById() throws Exception {
        // Given
        Novel novel = Novel.builder()
                .id(1L)
                .name("测试小说")
                .author("测试作者")
                .genre("玄幻")
                .build();

        when(novelService.getNovelById(1L)).thenReturn(novel);

        // When & Then
        mockMvc.perform(get("/api/novels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试小说"));
    }

    @Test
    void testDeleteNovel() throws Exception {
        // Given
        doNothing().when(novelService).deleteNovel(1L);

        // When & Then
        mockMvc.perform(delete("/api/novels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(novelService, times(1)).deleteNovel(1L);
    }

    @Test
    void testIndexNovel() throws Exception {
        // Given
        when(novelService.indexNovel(1L)).thenReturn(10);

        // When & Then
        mockMvc.perform(post("/api/novels/1/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chunkCount").value(10));

        verify(novelService, times(1)).indexNovel(1L);
    }

    @Test
    void testUploadNovel() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "测试内容".getBytes()
        );

        when(novelService.uploadNovel(any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(multipart("/api/novels/upload")
                        .file(file)
                        .param("name", "测试小说")
                        .param("author", "测试作者"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.novelId").value(1));
    }
}
