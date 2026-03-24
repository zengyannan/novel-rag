package com.novel.backend.crawler.integration;

import com.novel.backend.crawler.config.CrawlerConfig;
import com.novel.backend.crawler.fetcher.HttpFetcher;
import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.NovelSource;
import com.novel.backend.crawler.parser.BiqugeParser;
import com.novel.backend.crawler.strategy.AntiCrawlStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * 爬虫命令行测试工具
 * 可以手动测试爬虫功能
 */
public class CrawlerCli {

    public static void main(String[] args) {
        CrawlerConfig crawlerConfig = new CrawlerConfig();
        crawlerConfig.setEnabled(true);
        crawlerConfig.getRequest().setTimeout(30000);
        crawlerConfig.getRequest().setRetryTimes(3);
        crawlerConfig.getAntiCrawl().setRequestDelayMin(1000);
        crawlerConfig.getAntiCrawl().setRequestDelayMax(2000);

        AntiCrawlStrategy antiCrawlStrategy = new AntiCrawlStrategy(crawlerConfig);
        HttpFetcher httpFetcher = new HttpFetcher(crawlerConfig, antiCrawlStrategy);
        BiqugeParser biqugeParser = new BiqugeParser(httpFetcher);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== 小说爬虫测试工具 ===");
        System.out.println("1. 搜索小说");
        System.out.println("2. 直接抓取小说URL");
        System.out.println("3. 退出");
        System.out.print("请选择操作: ");

        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    searchAndCrawl(scanner, biqugeParser, antiCrawlStrategy);
                    break;
                case "2":
                    directCrawl(scanner, biqugeParser, antiCrawlStrategy);
                    break;
                case "3":
                    System.out.println("退出");
                    return;
                default:
                    System.out.println("无效选择");
            }
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void searchAndCrawl(Scanner scanner, BiqugeParser parser, AntiCrawlStrategy strategy) throws IOException {
        System.out.print("请输入小说名称: ");
        String novelName = scanner.nextLine().trim();

        System.out.println("正在搜索...");
        List<NovelSource> results = parser.search(novelName);

        if (results.isEmpty()) {
            System.out.println("未找到小说");
            return;
        }

        System.out.println("\n找到以下小说:");
        for (int i = 0; i < results.size(); i++) {
            NovelSource source = results.get(i);
            System.out.printf("%d. %s - %s (%d章)%n",
                i + 1, source.getNovelName(), source.getAuthor(), source.getChapterCount());
        }

        System.out.print("\n请选择要抓取的小说序号 (1-" + results.size() + "): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;

        if (index < 0 || index >= results.size()) {
            System.out.println("无效选择");
            return;
        }

        NovelSource selected = results.get(index);
        System.out.println("选择: " + selected.getNovelName());

        crawlNovel(selected, parser, strategy, scanner);
    }

    private static void directCrawl(Scanner scanner, BiqugeParser parser, AntiCrawlStrategy strategy) throws IOException {
        System.out.print("请输入小说目录页URL: ");
        String url = scanner.nextLine().trim();

        System.out.println("正在获取小说信息...");
        NovelSource novelInfo = parser.getNovelInfo(url);

        if (novelInfo == null) {
            System.out.println("无法获取小说信息");
            return;
        }

        System.out.println("小说: " + novelInfo.getNovelName() + " - " + novelInfo.getAuthor());
        crawlNovel(novelInfo, parser, strategy, scanner);
    }

    private static void crawlNovel(NovelSource source, BiqugeParser parser, AntiCrawlStrategy strategy, Scanner scanner) throws IOException {
        System.out.print("开始章节 (默认1): ");
        String startStr = scanner.nextLine().trim();
        int startChapter = startStr.isEmpty() ? 1 : Integer.parseInt(startStr);

        System.out.print("结束章节 (默认抓取前10章测试): ");
        String endStr = scanner.nextLine().trim();
        int endChapter = endStr.isEmpty() ? 10 : Integer.parseInt(endStr);

        System.out.println("\n开始抓取...");

        // 创建保存目录
        String safeName = source.getNovelName().replaceAll("[\\\\/:*?\"<>|]", "_");
        Path saveDir = Paths.get("novel-txt", safeName);
        Files.createDirectories(saveDir);
        System.out.println("保存目录: " + saveDir.toAbsolutePath());

        // 获取目录
        List<ChapterInfo> chapters = parser.getCatalog(source.getUrl());
        System.out.println("总章节数: " + chapters.size());

        endChapter = Math.min(endChapter, chapters.size());

        for (int i = startChapter - 1; i < endChapter; i++) {
            ChapterInfo chapter = chapters.get(i);

            strategy.randomDelay();
            String content = parser.getChapterContent(chapter.getUrl());

            if (!content.isEmpty()) {
                String fileName = String.format("%04d_%s.txt", chapter.getChapterNumber(),
                    chapter.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_"));
                Path filePath = saveDir.resolve(fileName);

                String fileContent = String.format("第%d章 %s%n%n%s",
                    chapter.getChapterNumber(), chapter.getTitle(), content);

                Files.writeString(filePath, fileContent);
                System.out.printf("已保存: %s (%d字)%n", fileName, content.length());
            } else {
                System.out.println("跳过空章节: " + chapter.getTitle());
            }
        }

        System.out.println("\n抓取完成！");
        System.out.println("文件保存在: " + saveDir.toAbsolutePath());
    }
}
