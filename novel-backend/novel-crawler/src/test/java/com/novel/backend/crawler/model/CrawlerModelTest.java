package com.novel.backend.crawler.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 爬虫模型类测试
 */
class CrawlerModelTest {

    @Test
    @DisplayName("ChapterInfo构建测试")
    void testChapterInfoBuilder() {
        ChapterInfo chapter = ChapterInfo.builder()
            .chapterNumber(1)
            .title("第一章 开端")
            .url("https://example.com/chapter/1")
            .content("章节内容...")
            .wordCount(1000)
            .build();

        assertEquals(1, chapter.getChapterNumber());
        assertEquals("第一章 开端", chapter.getTitle());
        assertEquals("https://example.com/chapter/1", chapter.getUrl());
        assertEquals("章节内容...", chapter.getContent());
        assertEquals(1000, chapter.getWordCount());
    }

    @Test
    @DisplayName("CrawlResult成功结果构建")
    void testCrawlResultSuccess() {
        List<ChapterInfo> chapters = Arrays.asList(
            ChapterInfo.builder().chapterNumber(1).title("第一章").build(),
            ChapterInfo.builder().chapterNumber(2).title("第二章").build()
        );

        CrawlResult result = CrawlResult.success("测试小说", "测试作者", "笔趣阁", chapters);

        assertTrue(result.getSuccess());
        assertNull(result.getErrorMessage());
        assertEquals("测试小说", result.getNovelName());
        assertEquals("测试作者", result.getAuthor());
        assertEquals("笔趣阁", result.getSourceSite());
        assertEquals(2, result.getTotalChapters());
        assertEquals(2, result.getChapters().size());
    }

    @Test
    @DisplayName("CrawlResult失败结果构建")
    void testCrawlResultFailure() {
        CrawlResult result = CrawlResult.failure("网络连接失败");

        assertFalse(result.getSuccess());
        assertEquals("网络连接失败", result.getErrorMessage());
        assertNull(result.getNovelName());
        assertNull(result.getChapters());
    }

    @Test
    @DisplayName("NovelSource构建测试")
    void testNovelSourceBuilder() {
        NovelSource source = NovelSource.builder()
            .sourceId("biquge_12345")
            .sourceName("笔趣阁")
            .novelName("斗破苍穹")
            .author("天蚕土豆")
            .chapterCount(1648)
            .url("https://example.com/novel/12345")
            .siteId("biquge")
            .build();

        assertEquals("biquge_12345", source.getSourceId());
        assertEquals("笔趣阁", source.getSourceName());
        assertEquals("斗破苍穹", source.getNovelName());
        assertEquals("天蚕土豆", source.getAuthor());
        assertEquals(1648, source.getChapterCount());
        assertEquals("biquge", source.getSiteId());
    }
}
