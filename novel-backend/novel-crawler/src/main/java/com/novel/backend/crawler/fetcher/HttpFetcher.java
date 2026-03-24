package com.novel.backend.crawler.fetcher;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * HTTP请求获取器
 * 使用Jsoup进行HTTP请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpFetcher {

    private final CrawlerConfig crawlerConfig;
    private final AntiCrawlStrategy antiCrawlStrategy;

    static {
        // 禁用SSL验证（仅用于测试环境）
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    @Override
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
            }, null);
            SSLContext.setDefault(context);

            // 禁用主机名验证
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            log.warn("无法禁用SSL验证: {}", e.getMessage());
        }
    }

    /**
     * 获取HTML文档
     *
     * @param url 目标URL
     * @return Jsoup Document
     */
    public Document fetch(String url) throws IOException {
        // 支持本地文件（file:// 或 file:/ 格式）
        if (url.startsWith("file://") || url.startsWith("file:/")) {
            return fetchLocalFile(url);
        }
        return fetchHttp(url, 0);
    }

    /**
     * 获取本地文件
     */
    private Document fetchLocalFile(String url) throws IOException {
        // 处理 file:// 或 file:/ 格式
        String filePath = url.replaceFirst("^file:/+", "/");
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        log.debug("读取本地文件: {}", filePath);
        return Jsoup.parse(file, StandardCharsets.UTF_8.name());
    }

    /**
     * 获取HTML文档（带重试）
     *
     * @param url        目标URL
     * @param retryCount 当前重试次数
     * @return Jsoup Document
     */
    private Document fetchHttp(String url, int retryCount) throws IOException {
        int maxRetries = crawlerConfig.getRequest().getRetryTimes();
        int timeout = crawlerConfig.getRequest().getTimeout();
        String userAgent = antiCrawlStrategy.getRandomUserAgent();

        try {
            log.debug("请求URL: {}, User-Agent: {}", url, userAgent);

            // 执行反爬延迟
            if (retryCount > 0) {
                int retryDelay = antiCrawlStrategy.calculateRetryDelay(retryCount - 1);
                antiCrawlStrategy.fixedDelay(retryDelay);
            }

            Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .timeout(timeout)
                .followRedirects(true)
                .ignoreContentType(true)
                .ignoreHttpErrors(false)
                .get();

            log.debug("请求成功: {}", url);
            return doc;

        } catch (IOException e) {
            log.warn("请求失败 (重试 {}/{}): {} - {}", retryCount, maxRetries, url, e.getMessage());

            if (retryCount < maxRetries) {
                return fetchHttp(url, retryCount + 1);
            }
            throw e;
        }
    }

    /**
     * 获取HTML字符串
     *
     * @param url 目标URL
     * @return HTML字符串
     */
    public String fetchHtml(String url) throws IOException {
        Document doc = fetch(url);
        return doc.html();
    }

    /**
     * 获取页面文本内容
     *
     * @param url 目标URL
     * @return 纯文本内容
     */
    public String fetchText(String url) throws IOException {
        Document doc = fetch(url);
        return doc.text();
    }

    /**
     * 检查URL是否可访问
     *
     * @param url 目标URL
     * @return 是否可访问
     */
    public boolean isAccessible(String url) {
        try {
            fetch(url);
            return true;
        } catch (Exception e) {
            log.warn("URL不可访问: {} - {}", url, e.getMessage());
            return false;
        }
    }
}
