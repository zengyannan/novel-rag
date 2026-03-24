package com.novel.backend.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 独立爬虫测试 - 不依赖Spring容器
 * 直接运行测试抓取功能
 */
public class StandaloneCrawlerTest {

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    };

    private static int userAgentIndex = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("小说爬虫测试 - 抓取完整小说");
        System.out.println("=".repeat(60));

        // 目标小说 (铅笔小说 - 日月永在)
        String bookUrl = "https://www.23qb.net/book/1121/";
        String baseUrl = "https://www.23qb.net";

        System.out.println("\n目标URL: " + bookUrl);

        // 1. 获取小说信息
        Document mainDoc = fetch(bookUrl);
        String novelName = extractNovelName(mainDoc);
        System.out.println("小说名: " + novelName);

        // 2. 获取目录页
        String catalogUrl = findCatalogUrl(mainDoc, baseUrl);
        System.out.println("目录页: " + catalogUrl);

        Document catalogDoc = fetch(catalogUrl);

        // 3. 提取章节列表
        List<ChapterLink> chapters = extractChapters(catalogDoc);
        System.out.println("章节数: " + chapters.size());

        if (chapters.isEmpty()) {
            System.err.println("无法获取章节列表!");
            return;
        }

        // 4. 创建保存目录
        String safeName = sanitizeFileName(novelName);
        Path saveDir = Paths.get("novel-txt", safeName);
        Files.createDirectories(saveDir);
        System.out.println("保存目录: " + saveDir.toAbsolutePath());

        // 5. 抓取所有章节
        System.out.println("\n开始抓取...");
        System.out.println("-".repeat(60));

        StringBuilder fullContent = new StringBuilder();
        fullContent.append("书名：").append(novelName).append("\n\n");

        int success = 0;
        int total = chapters.size();
        int maxChapters = Math.min(50, total); // 测试时只抓取前50章

        for (int i = 0; i < maxChapters; i++) {
            ChapterLink chapter = chapters.get(i);

            try {
                // 延迟
                Thread.sleep(300 + (long)(Math.random() * 200));

                String content = fetchChapterContent(chapter.url, baseUrl);

                if (!content.isEmpty()) {
                    int num = i + 1;
                    // 保存单章
                    String fileName = String.format("%04d_%s.txt", num, sanitizeFileName(chapter.title));
                    Path chapterFile = saveDir.resolve(fileName);
                    String chapterContent = "第" + num + "章 " + chapter.title + "\n\n" + content;
                    Files.writeString(chapterFile, chapterContent, StandardCharsets.UTF_8);

                    // 拼接完整内容
                    fullContent.append("第").append(num).append("章 ").append(chapter.title).append("\n\n");
                    fullContent.append(content).append("\n\n");

                    success++;

                    // 进度显示
                    if (num % 50 == 0 || num == 1 || num == maxChapters) {
                        System.out.printf("进度: %d/%d (%.1f%%)%n", num, maxChapters, num * 100.0 / maxChapters);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("\n用户中断");
                break;
            } catch (Exception e) {
                System.err.println("失败: " + chapter.title + " - " + e.getMessage());
            }
        }

        // 6. 保存完整版
        Path fullFile = saveDir.resolve(safeName + "_完整版.txt");
        Files.writeString(fullFile, fullContent.toString(), StandardCharsets.UTF_8);

        // 7. 输出结果
        System.out.println("-".repeat(60));
        System.out.println("抓取完成!");
        System.out.println("成功: " + success + "/" + maxChapters + " 章");
        System.out.println("单章目录: " + saveDir);
        System.out.println("完整版: " + fullFile);

        if (Files.exists(fullFile)) {
            long sizeMb = Files.size(fullFile) / 1024 / 1024;
            System.out.println("文件大小: " + sizeMb + " MB");
        }

        // 列出生成的文件
        File[] files = saveDir.toFile().listFiles();
        if (files != null) {
            System.out.println("\n生成 " + files.length + " 个文件");
        }
    }

    private static Document fetch(String url) throws IOException {
        return fetch(url, 3);
    }

    private static Document fetch(String url, int retries) throws IOException {
        String ua = USER_AGENTS[userAgentIndex % USER_AGENTS.length];
        userAgentIndex++;

        IOException lastError = null;
        for (int i = 0; i <= retries; i++) {
            try {
                return Jsoup.connect(url)
                    .userAgent(ua)
                    .timeout(30000)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .sslSocketFactory(javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory())
                    .get();
            } catch (IOException e) {
                lastError = e;
                if (i < retries) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        throw e;
                    }
                }
            }
        }
        throw lastError;
    }

    private static String extractNovelName(Document doc) {
        Element h1 = doc.selectFirst("h1.page-title");
        if (h1 != null) {
            String text = h1.text().trim();
            // 移除链接文字
            Element a = h1.selectFirst("a");
            if (a != null) {
                return a.text().trim();
            }
            return text;
        }
        return doc.title().replace("_铅笔小说", "").trim();
    }

    private static String findCatalogUrl(Document doc, String baseUrl) {
        Element catalogLink = doc.selectFirst("a[href*='catalog']");
        if (catalogLink != null) {
            String href = catalogLink.attr("href");
            if (href.startsWith("http")) {
                return href;
            }
            return baseUrl + href;
        }
        // 默认目录页
        return baseUrl + "/book/1121/catalog";
    }

    private static List<ChapterLink> extractChapters(Document doc) {
        List<ChapterLink> chapters = new ArrayList<>();

        // 匹配铅笔小说的章节链接格式
        Elements links = doc.select("a.module-row-text[href*='/book/']");

        for (Element link : links) {
            String href = link.attr("href");
            String title = link.attr("title");
            if (title.isEmpty()) {
                title = link.text().trim();
            }

            if (!href.isEmpty() && !title.isEmpty() && href.matches(".*/\\d+\\.html$")) {
                chapters.add(new ChapterLink(href, title));
            }
        }

        // 去重
        List<ChapterLink> unique = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ChapterLink ch : chapters) {
            if (!seen.contains(ch.url)) {
                seen.add(ch.url);
                unique.add(ch);
            }
        }

        return unique;
    }

    private static String fetchChapterContent(String url, String baseUrl) throws IOException {
        String fullUrl = url.startsWith("http") ? url : baseUrl + url;

        Document doc = fetch(fullUrl);

        // 铅笔小说内容在 div.article-content 中
        Element articleContent = doc.selectFirst("div.article-content");

        if (articleContent != null) {
            Elements paragraphs = articleContent.select("p");
            StringBuilder sb = new StringBuilder();
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            String content = sb.toString().trim();
            if (content.length() > 50) {
                return content;
            }
        }

        // 备选方案
        Elements paragraphs = doc.select("div.chapter-content p, article p");
        if (!paragraphs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString().trim();
        }

        return "";
    }

    private static String sanitizeFileName(String name) {
        if (name == null) return "untitled";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                   .replaceAll("\\s+", "_");
    }

    private static class ChapterLink {
        String url;
        String title;

        ChapterLink(String url, String title) {
            this.url = url;
            this.title = title;
        }
    }
}
