package com.novel.backend.crawler.strategy;

import com.novel.backend.crawler.config.CrawlerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 反爬策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AntiCrawlStrategy {

    private final CrawlerConfig crawlerConfig;
    private final Random random = new Random();
    private int currentUserAgentIndex = 0;

    /**
     * 获取随机User-Agent
     */
    public String getRandomUserAgent() {
        if (!crawlerConfig.getAntiCrawl().getUserAgentRotation()) {
            return CrawlerConfig.USER_AGENTS.get(0);
        }
        return CrawlerConfig.USER_AGENTS.get(random.nextInt(CrawlerConfig.USER_AGENTS.size()));
    }

    /**
     * 获取轮换User-Agent
     */
    public String getRotatingUserAgent() {
        if (!crawlerConfig.getAntiCrawl().getUserAgentRotation()) {
            return CrawlerConfig.USER_AGENTS.get(0);
        }
        currentUserAgentIndex = (currentUserAgentIndex + 1) % CrawlerConfig.USER_AGENTS.size();
        return CrawlerConfig.USER_AGENTS.get(currentUserAgentIndex);
    }

    /**
     * 执行随机延迟
     */
    public void randomDelay() {
        int minDelay = crawlerConfig.getAntiCrawl().getRequestDelayMin();
        int maxDelay = crawlerConfig.getAntiCrawl().getRequestDelayMax();

        if (minDelay <= 0 && maxDelay <= 0) {
            return;
        }

        int delay = random.nextInt(maxDelay - minDelay + 1) + minDelay;
        try {
            log.debug("执行反爬延迟: {}ms", delay);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("延迟被中断");
        }
    }

    /**
     * 执行固定延迟
     */
    public void fixedDelay(int delayMs) {
        try {
            log.debug("执行固定延迟: {}ms", delayMs);
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("延迟被中断");
        }
    }

    /**
     * 计算重试延迟（指数退避）
     */
    public int calculateRetryDelay(int retryCount) {
        int baseDelay = crawlerConfig.getRequest().getRetryDelay();
        // 指数退避，最大10秒
        return Math.min(baseDelay * (1 << retryCount), 10000);
    }

    /**
     * 是否启用代理
     */
    public boolean isProxyEnabled() {
        return crawlerConfig.getAntiCrawl().getProxyEnabled();
    }
}
