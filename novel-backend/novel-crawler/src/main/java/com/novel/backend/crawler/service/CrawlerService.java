package com.novel.backend.crawler.service;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.CrawlResult;
import com.novel.backend.crawler.model.NovelSource;
import com.novel.backend.crawler.parser.SiteParser;
import com.novel.backend.crawler.parser.SiteParserRegistry;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫调度服务
 * 协调各个组件完成小说抓取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final SiteParserRegistry parserRegistry;
    private final AntiCrawlStrategy antiCrawlStrategy;
    private final CrawlerConfig crawlerConfig;

    /**
     * 搜索小说
     *
     * @param keyword 搜索关键词
     * @return 小说来源列表
     */
    public List<NovelSource> searchNovel(String keyword) {
        if (!crawlerConfig.getEnabled()) {
            log.warn("爬虫模块已禁用");
            return new ArrayList<>();
        }

        log.info("开始搜索小说: {}", keyword);
        List<NovelSource> allResults = new ArrayList<>();

        // 使用笔趣阁解析器进行搜索
        parserRegistry.getParserById("biquge").ifPresent(parser -> {
            try {
                antiCrawlStrategy.randomDelay();
                List<NovelSource> results = parser.search(keyword);
                allResults.addAll(results);
                log.info("从 {} 搜索到 {} 本小说", parser.getSiteName(), results.size());
            } catch (Exception e) {
                log.error("从 {} 搜索失败: {}", parser.getSiteName(), e.getMessage());
            }
        });

        return allResults;
    }

    /**
     * 搜索小说（指定站点）
     *
     * @param keyword 搜索关键词
     * @param siteId  站点ID
     * @return 小说来源列表
     */
    public List<NovelSource> searchNovel(String keyword, String siteId) {
        if (!crawlerConfig.getEnabled()) {
            log.warn("爬虫模块已禁用");
            return new ArrayList<>();
        }

        return parserRegistry.getParserById(siteId)
            .map(parser -> {
                try {
                    antiCrawlStrategy.randomDelay();
                    return parser.search(keyword);
                } catch (IOException e) {
                    log.error("搜索失败: {}", e.getMessage());
                    return new ArrayList<NovelSource>();
                }
            })
            .orElse(new ArrayList<>());
    }

    /**
     * 获取小说目录
     *
     * @param novelUrl 小说URL
     * @return 章节列表
     */
    public List<ChapterInfo> getCatalog(String novelUrl) {
        if (!crawlerConfig.getEnabled()) {
            log.warn("爬虫模块已禁用");
            return new ArrayList<>();
        }

        log.info("获取小说目录: {}", novelUrl);
        SiteParser parser = parserRegistry.getParser(novelUrl);

        try {
            antiCrawlStrategy.randomDelay();
            return parser.getCatalog(novelUrl);
        } catch (IOException e) {
            log.error("获取目录失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取小说信息
     *
     * @param novelUrl 小说URL
     * @return 小说来源信息
     */
    public NovelSource getNovelInfo(String novelUrl) {
        if (!crawlerConfig.getEnabled()) {
            log.warn("爬虫模块已禁用");
            return null;
        }

        SiteParser parser = parserRegistry.getParser(novelUrl);

        try {
            antiCrawlStrategy.randomDelay();
            return parser.getNovelInfo(novelUrl);
        } catch (IOException e) {
            log.error("获取小说信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取章节内容
     *
     * @param chapterUrl 章节URL
     * @return 章节内容
     */
    public String getChapterContent(String chapterUrl) {
        if (!crawlerConfig.getEnabled()) {
            log.warn("爬虫模块已禁用");
            return "";
        }

        SiteParser parser = parserRegistry.getParser(chapterUrl);

        try {
            antiCrawlStrategy.randomDelay();
            return parser.getChapterContent(chapterUrl);
        } catch (IOException e) {
            log.error("获取章节内容失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 抓取小说（获取目录和部分章节）
     *
     * @param novelUrl 小说URL
     * @return 爬取结果
     */
    public CrawlResult crawlNovel(String novelUrl) {
        return crawlNovel(novelUrl, null, null, false);
    }

    /**
     * 抓取小说（指定章节范围）
     *
     * @param novelUrl    小说URL
     * @param startChapter 起始章节（从1开始）
     * @param endChapter   结束章节
     * @param storeContent 是否存储内容
     * @return 爬取结果
     */
    public CrawlResult crawlNovel(String novelUrl, Integer startChapter, Integer endChapter, boolean storeContent) {
        if (!crawlerConfig.getEnabled()) {
            return CrawlResult.failure("爬虫模块已禁用");
        }

        log.info("开始抓取小说: {}, 章节 {}-{}", novelUrl, startChapter, endChapter);

        try {
            SiteParser parser = parserRegistry.getParser(novelUrl);

            // 获取小说信息
            antiCrawlStrategy.randomDelay();
            NovelSource novelInfo = parser.getNovelInfo(novelUrl);
            if (novelInfo == null) {
                return CrawlResult.failure("无法获取小说信息");
            }

            // 获取目录
            antiCrawlStrategy.randomDelay();
            List<ChapterInfo> chapters = parser.getCatalog(novelUrl);
            if (chapters.isEmpty()) {
                return CrawlResult.failure("无法获取章节目录");
            }

            // 确定章节范围
            int start = startChapter != null ? Math.max(1, startChapter) : 1;
            int end = endChapter != null ? Math.min(endChapter, chapters.size()) : chapters.size();

            List<ChapterInfo> crawledChapters = new ArrayList<>();

            // 抓取章节内容
            for (int i = start - 1; i < end; i++) {
                ChapterInfo chapter = chapters.get(i);

                try {
                    antiCrawlStrategy.randomDelay();
                    String content = parser.getChapterContent(chapter.getUrl());

                    if (storeContent && !content.isEmpty()) {
                        chapter.setContent(content);
                        chapter.setWordCount(content.length());
                    }

                    crawledChapters.add(chapter);
                    log.info("已抓取章节 {}/{}: {}", i + 1, end, chapter.getTitle());

                } catch (Exception e) {
                    log.error("抓取章节失败: {} - {}", chapter.getTitle(), e.getMessage());
                    // 继续抓取下一章
                }
            }

            log.info("小说抓取完成: {} 章", crawledChapters.size());

            return CrawlResult.builder()
                .success(true)
                .novelName(novelInfo.getNovelName())
                .author(novelInfo.getAuthor())
                .sourceSite(novelInfo.getSourceName())
                .chapters(crawledChapters)
                .totalChapters(chapters.size())
                .build();

        } catch (Exception e) {
            log.error("抓取小说失败: {}", e.getMessage());
            return CrawlResult.failure("抓取失败: " + e.getMessage());
        }
    }
}
