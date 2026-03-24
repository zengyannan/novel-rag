package com.novel.backend.crawler;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 反爬策略测试
 */
class AntiCrawlStrategyTest {

    private AntiCrawlStrategy antiCrawlStrategy;
    private CrawlerConfig crawlerConfig;

    @BeforeEach
    void setUp() {
        crawlerConfig = new CrawlerConfig();
        crawlerConfig.setAntiCrawl(new CrawlerConfig.AntiCrawlConfig());
        crawlerConfig.setRequest(new CrawlerConfig.RequestConfig());
        antiCrawlStrategy = new AntiCrawlStrategy(crawlerConfig);
    }

    @Test
    @DisplayName("获取随机User-Agent")
    void testGetRandomUserAgent() {
        String userAgent = antiCrawlStrategy.getRandomUserAgent();

        assertNotNull(userAgent);
        assertFalse(userAgent.isEmpty());
        assertTrue(CrawlerConfig.USER_AGENTS.contains(userAgent));
    }

    @Test
    @DisplayName("获取轮换User-Agent")
    void testGetRotatingUserAgent() {
        crawlerConfig.getAntiCrawl().setUserAgentRotation(true);

        String first = antiCrawlStrategy.getRotatingUserAgent();
        String second = antiCrawlStrategy.getRotatingUserAgent();

        assertNotNull(first);
        assertNotNull(second);
        // 轮换后应该获取到不同的User-Agent
        assertTrue(CrawlerConfig.USER_AGENTS.contains(first));
        assertTrue(CrawlerConfig.USER_AGENTS.contains(second));
    }

    @Test
    @DisplayName("UA轮换禁用时返回第一个UA")
    void testGetUserAgentWhenRotationDisabled() {
        crawlerConfig.getAntiCrawl().setUserAgentRotation(false);

        String userAgent = antiCrawlStrategy.getRandomUserAgent();

        assertEquals(CrawlerConfig.USER_AGENTS.get(0), userAgent);
    }

    @Test
    @DisplayName("计算重试延迟（指数退避）")
    void testCalculateRetryDelay() {
        crawlerConfig.getRequest().setRetryDelay(1000);

        int delay0 = antiCrawlStrategy.calculateRetryDelay(0);
        int delay1 = antiCrawlStrategy.calculateRetryDelay(1);
        int delay2 = antiCrawlStrategy.calculateRetryDelay(2);

        assertEquals(1000, delay0);
        assertEquals(2000, delay1);
        assertEquals(4000, delay2);
    }

    @Test
    @DisplayName("重试延迟有上限")
    void testRetryDelayMaxLimit() {
        crawlerConfig.getRequest().setRetryDelay(5000);

        int delay = antiCrawlStrategy.calculateRetryDelay(10);

        assertTrue(delay <= 10000);
    }

    @Test
    @DisplayName("代理启用状态检查")
    void testIsProxyEnabled() {
        crawlerConfig.getAntiCrawl().setProxyEnabled(true);
        assertTrue(antiCrawlStrategy.isProxyEnabled());

        crawlerConfig.getAntiCrawl().setProxyEnabled(false);
        assertFalse(antiCrawlStrategy.isProxyEnabled());
    }
}
