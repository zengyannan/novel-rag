package com.novel.backend.crawler;

import com.novel.backend.crawler.config.CrawlerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 爬虫配置测试
 */
class CrawlerConfigTest {

    @Test
    @DisplayName("默认配置值测试")
    void testDefaultConfigValues() {
        CrawlerConfig config = new CrawlerConfig();

        assertTrue(config.getEnabled());
        assertNotNull(config.getRequest());
        assertNotNull(config.getAntiCrawl());
        assertNotNull(config.getStorage());

        assertEquals(30000, config.getRequest().getTimeout());
        assertEquals(3, config.getRequest().getRetryTimes());
        assertEquals(2000, config.getRequest().getRetryDelay());

        assertTrue(config.getAntiCrawl().getUserAgentRotation());
        assertEquals(1000, config.getAntiCrawl().getRequestDelayMin());
        assertEquals(3000, config.getAntiCrawl().getRequestDelayMax());

        assertFalse(config.getStorage().getStoreContent());
        assertEquals(30, config.getStorage().getContentExpireDays());
    }

    @Test
    @DisplayName("User-Agent池不为空")
    void testUserAgentsPool() {
        assertFalse(CrawlerConfig.USER_AGENTS.isEmpty());
        assertTrue(CrawlerConfig.USER_AGENTS.size() >= 4);

        for (String ua : CrawlerConfig.USER_AGENTS) {
            assertTrue(ua.contains("Mozilla"));
        }
    }
}
