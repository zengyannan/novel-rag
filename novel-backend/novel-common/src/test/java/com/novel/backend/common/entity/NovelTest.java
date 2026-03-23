package com.novel.backend.common.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Novel 实体类测试
 */
class NovelTest {

    @Test
    void testNovelBuilder() {
        // Given
        String name = "斗破苍穹";
        String author = "天蚕土豆";
        String genre = "玄幻";
        String tags = "热血,升级";

        // When
        Novel novel = Novel.builder()
                .name(name)
                .author(author)
                .genre(genre)
                .tags(tags)
                .wordCount(5000000)
                .chapterCount(1648)
                .build();

        // Then
        assertEquals(name, novel.getName());
        assertEquals(author, novel.getAuthor());
        assertEquals(genre, novel.getGenre());
        assertEquals(tags, novel.getTags());
        assertEquals(5000000, novel.getWordCount());
        assertEquals(1648, novel.getChapterCount());
    }

    @Test
    void testOnCreate() {
        // Given
        Novel novel = new Novel();
        novel.setName("测试小说");

        // When
        novel.onCreate();

        // Then
        assertNotNull(novel.getCreatedAt());
        assertNotNull(novel.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        // Given
        Novel novel = new Novel();
        novel.setName("测试小说");
        novel.onCreate();
        LocalDateTime originalCreatedAt = novel.getCreatedAt();

        // When
        try {
            Thread.sleep(10); // 确保时间差
        } catch (InterruptedException e) {
            // ignore
        }
        novel.onUpdate();

        // Then
        assertEquals(originalCreatedAt, novel.getCreatedAt());
        assertTrue(novel.getUpdatedAt().isAfter(originalCreatedAt) ||
                   novel.getUpdatedAt().equals(originalCreatedAt));
    }

    @Test
    void testNoArgsConstructor() {
        // When
        Novel novel = new Novel();

        // Then
        assertNull(novel.getName());
        assertNull(novel.getId());
    }

    @Test
    void testAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        Novel novel = new Novel(
                1L,
                "测试小说",
                "测试作者",
                "玄幻",
                "热血",
                "/path/to/file.txt",
                "txt",
                100000,
                100,
                "简介内容",
                now,
                now
        );

        // Then
        assertEquals(1L, novel.getId());
        assertEquals("测试小说", novel.getName());
        assertEquals("测试作者", novel.getAuthor());
        assertEquals("玄幻", novel.getGenre());
        assertEquals("热血", novel.getTags());
        assertEquals("/path/to/file.txt", novel.getFilePath());
        assertEquals("txt", novel.getFileType());
        assertEquals(100000, novel.getWordCount());
        assertEquals(100, novel.getChapterCount());
        assertEquals("简介内容", novel.getDescription());
    }
}
