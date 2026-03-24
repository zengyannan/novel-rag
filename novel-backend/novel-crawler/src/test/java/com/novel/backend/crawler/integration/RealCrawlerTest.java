package com.novel.backend.crawler.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 独立爬虫测试 - 不依赖Spring容器
 */
public class RealCrawlerTest {

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
    };

    public static void main(String[] args) throws Exception {
        System.out.println("=== 小说爬虫测试 ===\n");

        // 测试多个小说网站
        String[] testSites = {
            "https://www.xbiquge.la/book/10168/",  // 笔趣阁 - 凡人修仙传
            "https://www.xbiquge.la/book/75/",     // 笔趣阁 - 斗破苍穹
        };

        String baseUrl = null;
        String novelName = null;
        List<ChapterInfo> chapters = null;

        // 尝试访问测试站点
        for (String url : testSites) {
            try {
                System.out.println("尝试访问: " + url);
                Document doc = fetch(url);
                if (doc != null) {
                    novelName = extractNovelName(doc);
                    chapters = extractChapters(doc, url);
                    baseUrl = url;
                    System.out.println("成功! 小说名: " + novelName + ", 章节数: " + chapters.size());
                    break;
                }
            } catch (Exception e) {
                System.out.println("失败: " + e.getMessage());
            }
        }

        if (chapters == null || chapters.isEmpty()) {
            System.out.println("\n无法获取章节列表，尝试直接搜索...");
            // 尝试搜索
            String searchUrl = "https://www.xbiquge.la/modules/article/waps.php?searchkey=" + java.net.URLEncoder.encode("凡人修仙传", "UTF-8");
            System.out.println("搜索URL: " + searchUrl);
            Document searchDoc = fetch(searchUrl);
            if (searchDoc != null) {
                System.out.println("搜索页面内容长度: " + searchDoc.html().length());
                // 解析搜索结果
                Elements results = searchDoc.select(".grid tr");
                System.out.println("搜索结果数: " + results.size());
            }
            return;
        }

        // 创建保存目录
        String safeName = novelName.replaceAll("[\\\\/:*?\"<>|]", "_");
        Path saveDir = Paths.get("novel-txt", safeName);
        Files.createDirectories(saveDir);
        System.out.println("\n保存目录: " + saveDir.toAbsolutePath());

        // 抓取所有章节内容
        int totalChapters = chapters.size();
        int successCount = 0;
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("书名：").append(novelName).append("\n\n");

        System.out.println("\n开始抓取 " + totalChapters + " 章...");
        System.out.println("=".repeat(50));

        for (int i = 0; i < chapters.size(); i++) {
            ChapterInfo chapter = chapters.get(i);

            try {
                // 延迟避免被封
                Thread.sleep(500 + (long)(Math.random() * 500));

                String content = fetchChapterContent(chapter.url);

                if (!content.isEmpty()) {
                    // 保存单章节文件
                    String fileName = String.format("%04d_%s.txt", chapter.number, sanitizeFileName(chapter.title));
                    Path chapterFile = saveDir.resolve(fileName);
                    String chapterContent = "第" + chapter.number + "章 " + chapter.title + "\n\n" + content;
                    Files.writeString(chapterFile, chapterContent, StandardCharsets.UTF_8);

                    // 拼接到完整内容
                    fullContent.append("第").append(chapter.number).append("章 ").append(chapter.title).append("\n\n");
                    fullContent.append(content).append("\n\n");

                    successCount++;
                    if ((i + 1) % 10 == 0 || i == 0 || i == chapters.size() - 1) {
                        System.out.printf("进度: %d/%d (%.1f%%) - %s%n",
                            i + 1, totalChapters, (i + 1) * 100.0 / totalChapters, chapter.title);
                    }
                }
            } catch (Exception e) {
                System.err.println("抓取失败: " + chapter.title + " - " + e.getMessage());
            }
        }

        // 保存完整小说文件
        Path fullFile = saveDir.resolve(safeName + "_完整版.txt");
        Files.writeString(fullFile, fullContent.toString(), StandardCharsets.UTF_8);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("抓取完成!");
        System.out.println("成功: " + successCount + "/" + totalChapters + " 章");
        System.out.println("单章文件目录: " + saveDir);
        System.out.println("完整版文件: " + fullFile);
    }

    private static Document fetch(String url) throws IOException {
        String ua = USER_AGENTS[(int)(Math.random() * USER_AGENTS.length)];
        return Jsoup.connect(url)
            .userAgent(ua)
            .timeout(30000)
            .followRedirects(true)
            .ignoreContentType(true)
            .get();
    }

    private static String extractNovelName(Document doc) {
        Element h1 = doc.selectFirst("#info h1");
        if (h1 != null) {
            return h1.text().trim();
        }
        return doc.title().replace("_笔趣阁", "").replace("-笔趣阁", "").trim();
    }

    private static List<ChapterInfo> extractChapters(Document doc, String baseUrl) {
        List<ChapterInfo> chapters = new ArrayList<>();

        // 笔趣阁章节选择器
        String[] selectors = {
            "#list dl dd a",
            "#list a",
            ".listmain dl dd a"
        };

        Elements links = new Elements();
        for (String selector : selectors) {
            links = doc.select(selector);
            if (!links.isEmpty()) {
                System.out.println("使用选择器: " + selector);
                break;
            }
        }

        int num = 1;
        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.isEmpty()) {
                href = baseUrl + link.attr("href").replace("./", "");
            }
            String title = link.text().trim();

            if (!href.isEmpty() && !title.isEmpty()) {
                chapters.add(new ChapterInfo(num++, title, href));
            }
        }

        return chapters;
    }

    private static String fetchChapterContent(String url) throws IOException {
        Document doc = fetch(url);

        String[] contentSelectors = {
            "#content",
            "#chaptercontent",
            ".chapter-content",
            ".content"
        };

        for (String selector : contentSelectors) {
            Element content = doc.selectFirst(selector);
            if (content != null) {
                content.select("script, style, .ad, .ads, a").remove();

                String html = content.html();
                // 将<br>转换为换行
                html = html.replaceAll("<br\\s*/?>", "\n");
                // 移除HTML标签
                html = html.replaceAll("<[^>]+>", "");
                // 清理
                html = html.replaceAll("&nbsp;", " ")
                           .replaceAll("\\s+", " ")
                           .trim();

                if (html.length() > 100) {
                    return html;
                }
            }
        }

        return "";
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                   .replaceAll("\\s+", "_")
                   .substring(0, Math.min(name.length(), 30));
    }

    private static class ChapterInfo {
        int number;
        String title;
        String url;

        ChapterInfo(int number, String title, String url) {
            this.number = number;
            this.title = title;
            this.url = url;
        }
    }
}
