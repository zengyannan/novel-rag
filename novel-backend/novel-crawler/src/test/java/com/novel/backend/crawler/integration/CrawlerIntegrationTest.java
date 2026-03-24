package com.novel.backend.crawler.integration;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.fetcher.HttpFetcher;
import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.CrawlResult;
import com.novel.backend.crawler.model.NovelSource;
import com.novel.backend.crawler.parser.BiqugeParser;
import com.novel.backend.crawler.parser.SiteParser;
import com.novel.backend.crawler.parser.SiteParserRegistry;
import com.novel.backend.crawler.service.CrawlerService;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 爬虫集成测试
 * 需要网络连接
 */
class CrawlerIntegrationTest {

    private CrawlerConfig crawlerConfig;
    private AntiCrawlStrategy antiCrawlStrategy;
    private HttpFetcher httpFetcher;
    private BiqugeParser biqugeParser;

    @BeforeEach
    void setUp() {
        crawlerConfig = new CrawlerConfig();
        crawlerConfig.setEnabled(true);
        crawlerConfig.getRequest().setTimeout(30000);
        crawlerConfig.getRequest().setRetryTimes(3);
        crawlerConfig.getAntiCrawl().setRequestDelayMin(500);
        crawlerConfig.getAntiCrawl().setRequestDelayMax(1000);

        antiCrawlStrategy = new AntiCrawlStrategy(crawlerConfig);
        httpFetcher = new HttpFetcher(crawlerConfig, antiCrawlStrategy);
        biqugeParser = new BiqugeParser(httpFetcher);
    }

    @Test
    @DisplayName("测试HTTP请求获取器")
    void testHttpFetcher() throws IOException {
        String testUrl = "https://www.biquge.com.cn";

        boolean accessible = httpFetcher.isAccessible(testUrl);

        // 如果网络不可用，跳过测试
        if (!accessible) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        assertTrue(accessible, "应该能访问笔趣阁网站");

        String html = httpFetcher.fetchHtml(testUrl);
        assertNotNull(html);
        assertFalse(html.isEmpty());
        System.out.println("成功获取HTML，长度: " + html.length());
    }

    @Test
    @DisplayName("测试笔趣阁搜索")
    void testBiqugeSearch() throws IOException {
        if (!httpFetcher.isAccessible("https://www.biquge.com.cn")) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        List<NovelSource> results = biqugeParser.search("斗破苍穹");

        System.out.println("搜索结果数量: " + results.size());
        for (NovelSource source : results) {
            System.out.println("- " + source.getNovelName() + " - " + source.getAuthor() + " (" + source.getChapterCount() + "章)");
        }

        // 搜索应该有结果
        assertFalse(results.isEmpty(), "搜索应该有结果");
    }

    @Test
    @DisplayName("测试获取小说目录")
    void testGetCatalog() throws IOException {
        if (!httpFetcher.isAccessible("https://www.biquge.com.cn")) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        // 先搜索获取小说URL
        List<NovelSource> results = biqugeParser.search("斗破苍穹");
        if (results.isEmpty()) {
            System.out.println("搜索无结果，跳过测试");
            return;
        }

        String novelUrl = results.get(0).getUrl();
        System.out.println("小说URL: " + novelUrl);

        // 获取目录
        List<ChapterInfo> chapters = biqugeParser.getCatalog(novelUrl);

        System.out.println("章节数量: " + chapters.size());
        for (int i = 0; i < Math.min(5, chapters.size()); i++) {
            ChapterInfo ch = chapters.get(i);
            System.out.println("  " + ch.getChapterNumber() + ". " + ch.getTitle());
        }

        assertFalse(chapters.isEmpty(), "目录应该有章节");
    }

    @Test
    @DisplayName("测试获取章节内容")
    void testGetChapterContent() throws IOException {
        if (!httpFetcher.isAccessible("https://www.biquge.com.cn")) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        // 先搜索获取小说URL
        List<NovelSource> results = biqugeParser.search("斗破苍穹");
        if (results.isEmpty()) {
            System.out.println("搜索无结果，跳过测试");
            return;
        }

        String novelUrl = results.get(0).getUrl();
        List<ChapterInfo> chapters = biqugeParser.getCatalog(novelUrl);

        if (chapters.isEmpty()) {
            System.out.println("无章节，跳过测试");
            return;
        }

        // 获取第一章内容
        String chapterUrl = chapters.get(0).getUrl();
        System.out.println("章节URL: " + chapterUrl);

        antiCrawlStrategy.randomDelay();
        String content = biqugeParser.getChapterContent(chapterUrl);

        System.out.println("内容长度: " + content.length());
        System.out.println("内容预览: " + content.substring(0, Math.min(100, content.length())) + "...");

        assertFalse(content.isEmpty(), "章节内容不应为空");
    }

    @Test
    @DisplayName("测试完整爬取流程并保存到文件")
    void testFullCrawlAndSave() throws IOException {
        if (!httpFetcher.isAccessible("https://www.biquge.com.cn")) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        // 搜索小说
        String novelName = "斗破苍穹";
        List<NovelSource> results = biqugeParser.search(novelName);

        if (results.isEmpty()) {
            System.out.println("搜索无结果，跳过测试");
            return;
        }

        NovelSource novelSource = results.get(0);
        System.out.println("找到小说: " + novelSource.getNovelName() + " - " + novelSource.getAuthor());

        // 获取目录
        String novelUrl = novelSource.getUrl();
        List<ChapterInfo> chapters = biqugeParser.getCatalog(novelUrl);
        System.out.println("总章节数: " + chapters.size());

        // 创建保存目录
        Path saveDir = Paths.get("novel-txt", novelName.replaceAll("[\\\\/:*?\"<>|]", "_"));
        if (!Files.exists(saveDir)) {
            Files.createDirectories(saveDir);
        }
        System.out.println("保存目录: " + saveDir.toAbsolutePath());

        // 只爬取前3章作为测试
        int maxChapters = Math.min(3, chapters.size());
        for (int i = 0; i < maxChapters; i++) {
            ChapterInfo chapter = chapters.get(i);

            antiCrawlStrategy.randomDelay();
            String content = biqugeParser.getChapterContent(chapter.getUrl());

            if (!content.isEmpty()) {
                // 保存到文件
                String fileName = String.format("%04d_%s.txt", chapter.getChapterNumber(),
                    chapter.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_"));
                Path filePath = saveDir.resolve(fileName);

                String fileContent = String.format("第%d章 %s\n\n%s",
                    chapter.getChapterNumber(), chapter.getTitle(), content);

                Files.writeString(filePath, fileContent);
                System.out.println("保存: " + fileName + " (" + content.length() + "字)");
            }
        }

        System.out.println("\n爬取完成！文件保存在: " + saveDir.toAbsolutePath());

        // 验证文件已创建
        long fileCount = Files.list(saveDir).count();
        assertTrue(fileCount > 0, "应该有文件被创建");
    }
}
