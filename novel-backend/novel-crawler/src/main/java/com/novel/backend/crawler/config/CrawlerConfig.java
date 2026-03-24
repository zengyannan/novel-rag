package com.novel.backend.crawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 爬虫配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 请求配置
     */
    private RequestConfig request = new RequestConfig();

    /**
     * 反爬配置
     */
    private AntiCrawlConfig antiCrawl = new AntiCrawlConfig();

    /**
     * 存储配置
     */
    private StorageConfig storage = new StorageConfig();

    /**
     * 请求配置
     */
    @Data
    public static class RequestConfig {
        /**
         * 请求超时(毫秒)
         */
        private Integer timeout = 30000;

        /**
         * 重试次数
         */
        private Integer retryTimes = 3;

        /**
         * 重试延迟(毫秒)
         */
        private Integer retryDelay = 2000;
    }

    /**
     * 反爬配置
     */
    @Data
    public static class AntiCrawlConfig {
        /**
         * UA轮换
         */
        private Boolean userAgentRotation = true;

        /**
         * 最小请求延迟(毫秒)
         */
        private Integer requestDelayMin = 1000;

        /**
         * 最大请求延迟(毫秒)
         */
        private Integer requestDelayMax = 3000;

        /**
         * 代理启用
         */
        private Boolean proxyEnabled = false;
    }

    /**
     * 存储配置
     */
    @Data
    public static class StorageConfig {
        /**
         * 是否存储正文内容
         */
        private Boolean storeContent = false;

        /**
         * 内容缓存过期天数
         */
        private Integer contentExpireDays = 30;
    }

    /**
     * User-Agent池
     */
    public static final List<String> USER_AGENTS = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15"
    );
}
