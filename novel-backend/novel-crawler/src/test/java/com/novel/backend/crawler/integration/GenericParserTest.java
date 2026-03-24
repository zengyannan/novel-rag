package com.novel.backend.crawler.integration;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.fetcher.HttpFetcher;
import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.parser.GenericParser;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用公开可访问的网站测试爬虫功能
 */
class GenericParserTest {

    private CrawlerConfig crawlerConfig;
    private AntiCrawlStrategy antiCrawlStrategy;
    private HttpFetcher httpFetcher;
    private GenericParser genericParser;

    @BeforeEach
    void setUp() {
        crawlerConfig = new CrawlerConfig();
        crawlerConfig.setEnabled(true);
        crawlerConfig.getRequest().setTimeout(30000);
        crawlerConfig.getRequest().setRetryTimes(3);

        antiCrawlStrategy = new AntiCrawlStrategy(crawlerConfig);
        httpFetcher = new HttpFetcher(crawlerConfig, antiCrawlStrategy);
        genericParser = new GenericParser(httpFetcher);
    }

    @Test
    @DisplayName("测试获取公开网页内容")
    void testFetchPublicWebsite() throws IOException {
        // 使用一个稳定的测试网站
        String testUrl = "https://www.example.com";

        boolean accessible = httpFetcher.isAccessible(testUrl);
        if (!accessible) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        assertTrue(accessible, "应该能访问测试网站");

        Document doc = httpFetcher.fetch(testUrl);
        assertNotNull(doc);
        assertFalse(doc.text().isEmpty());

        System.out.println("成功获取内容: " + doc.title());
        System.out.println("内容长度: " + doc.text().length());
    }

    @Test
    @DisplayName("测试通用解析器supports方法")
    void testGenericParserSupports() {
        assertTrue(genericParser.supports("https://example.com"));
        assertTrue(genericParser.supports("http://example.com"));
        assertFalse(genericParser.supports("ftp://example.com"));
        assertFalse(genericParser.supports(null));
    }

    @Test
    @DisplayName("测试通用解析器获取目录")
    void testGenericParserGetCatalog() throws IOException {
        if (!httpFetcher.isAccessible("https://www.example.com")) {
            System.out.println("网络不可用，跳过测试");
            return;
        }

        // example.com 是简单网页，可能没有章节链接
        // 这个测试主要是验证解析器能正常工作
        var chapters = genericParser.getCatalog("https://www.example.com");
        assertNotNull(chapters);
        System.out.println("找到链接数量: " + chapters.size());
    }
}
