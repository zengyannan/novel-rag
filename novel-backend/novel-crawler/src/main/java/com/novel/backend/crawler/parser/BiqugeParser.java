package com.novel.backend.crawler.parser;

import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.NovelSource;
import com.novel.backend.crawler.fetcher.HttpFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 笔趣阁站点解析器
 * 支持多个笔趣阁镜像站点
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class BiqugeParser implements SiteParser {

    private final HttpFetcher httpFetcher;

    /**
     * 支持的笔趣阁域名列表
     */
    private static final List<String> SUPPORTED_DOMAINS = List.of(
        "biquge.com.cn",
        "www.biquge.com.cn",
        "biquge5200.com",
        "www.biquge5200.com",
        "bqg99.com",
        "www.bqg99.com",
        "xbiquge.so",
        "www.xbiquge.so",
        "xbiquge.la",
        "www.xbiquge.la",
        "biduo.cc",
        "www.biduo.cc",
        "ddyueshu.com",
        "www.ddyueshu.com",
        "ffxs8.com",
        "www.ffxs8.com",
        "zwdu.com",
        "www.zwdu.com"
    );

    /**
     * 搜索URL模板 - 使用多个镜像站点
     */
    private static final String[] SEARCH_URL_TEMPLATES = {
        "https://www.biquge5200.com/modules/article/search.php?searchkey=%s",
        "https://www.xbiquge.so/search.php?keyword=%s",
        "https://www.bqg99.com/search.php?q=%s",
        "https://www.biquge.com.cn/search.php?q=%s"
    };

    @Override
    public String getSiteId() {
        return "biquge";
    }

    @Override
    public String getSiteName() {
        return "笔趣阁";
    }

    @Override
    public List<NovelSource> search(String keyword) throws IOException {
        List<NovelSource> results = new ArrayList<>();

        // 尝试多个镜像站点
        for (String template : SEARCH_URL_TEMPLATES) {
            String searchUrl = String.format(template, keyword);
            log.info("搜索小说: {}", searchUrl);

            try {
                Document doc = httpFetcher.fetch(searchUrl);

                // 解析搜索结果 - 多种选择器适配不同站点
                Elements resultElements = doc.select(".result-list .result-item");

                if (resultElements.isEmpty()) {
                    // 尝试其他选择器（适配不同站点）
                    resultElements = doc.select(".grid .text-overflow, .search-result-list .search-result-item");
                }
                if (resultElements.isEmpty()) {
                    // xbiquge.so 的选择器
                    resultElements = doc.select(".novel-list .novel-item");
                }
                if (resultElements.isEmpty()) {
                    // bqg99.com 的选择器
                    resultElements = doc.select(".result-list li, .search-list li, .book-list li");
                }
                if (resultElements.isEmpty()) {
                    // 通用选择器 - 查找包含链接的列表项
                    resultElements = doc.select("ul li:has(a)");
                }

                for (Element item : resultElements) {
                    try {
                        NovelSource source = parseSearchResultItem(item);
                        if (source != null) {
                            results.add(source);
                        }
                    } catch (Exception e) {
                        log.warn("解析搜索结果项失败: {}", e.getMessage());
                    }
                }

                // 如果搜索结果为空，尝试检查是否直接跳转到小说页面
                if (results.isEmpty()) {
                    Element novelInfo = doc.selectFirst("#info");
                    if (novelInfo != null) {
                        NovelSource source = getNovelInfo(doc.location());
                        if (source != null) {
                            results.add(source);
                        }
                    }
                }

                if (!results.isEmpty()) {
                    log.info("从 {} 搜索到 {} 本小说", searchUrl, results.size());
                    return results;
                }
            } catch (Exception e) {
                log.warn("搜索站点失败: {} - {}", searchUrl, e.getMessage());
            }
        }

        log.info("搜索到 {} 本小说", results.size());
        return results;
    }

    /**
     * 解析单个搜索结果
     */
    private NovelSource parseSearchResultItem(Element item) {
        Element titleLink = item.selectFirst("a");
        if (titleLink == null) {
            return null;
        }

        String novelUrl = titleLink.absUrl("href");
        String novelName = titleLink.text().trim();

        if (novelUrl.isEmpty() || novelName.isEmpty()) {
            return null;
        }

        // 尝试提取作者
        String author = "";
        Element authorElement = item.selectFirst(".result-game-item-info-tag span");
        if (authorElement != null) {
            author = authorElement.text().trim();
        }
        if (author.isEmpty()) {
            authorElement = item.selectFirst(".author, .game-author");
            if (authorElement != null) {
                author = authorElement.text().replace("作者：", "").trim();
            }
        }

        // 尝试提取章节数
        int chapterCount = 0;
        Element countElement = item.selectFirst(".result-game-item-info-tag:last-child span");
        if (countElement != null) {
            try {
                chapterCount = Integer.parseInt(countElement.text().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                // 忽略
            }
        }

        return NovelSource.builder()
            .sourceId("biquge_" + extractNovelId(novelUrl))
            .sourceName(getSiteName())
            .novelName(novelName)
            .author(author)
            .chapterCount(chapterCount)
            .url(novelUrl)
            .siteId(getSiteId())
            .build();
    }

    @Override
    public List<ChapterInfo> getCatalog(String novelUrl) throws IOException {
        log.info("获取小说目录: {}", novelUrl);
        Document doc = httpFetcher.fetch(novelUrl);
        List<ChapterInfo> chapters = new ArrayList<>();

        // 笔趣阁常见的章节列表选择器
        String[] selectors = {
            "#list dl dd a",
            "#list a",
            ".listmain dl dd a",
            "#chapterList a",
            ".chapter-list a",
            "dl dd a",
            ".list dl a",
            "#chapters a",
            ".chapter a",
            "ul.list a"
        };

        Elements chapterLinks = new Elements();
        for (String selector : selectors) {
            chapterLinks = doc.select(selector);
            if (!chapterLinks.isEmpty()) {
                log.info("使用选择器 '{}' 找到 {} 个章节链接", selector, chapterLinks.size());
                break;
            }
        }

        // 如果没有找到，尝试调试输出
        if (chapterLinks.isEmpty()) {
            log.info("页面标题: {}", doc.title());
            log.info("页面 body 前500字符: {}", doc.body().html().substring(0, Math.min(500, doc.body().html().length())));
        }

        int chapterNum = 1;
        for (Element link : chapterLinks) {
            String href = link.absUrl("href");
            String title = link.text().trim();

            if (!href.isEmpty() && !title.isEmpty()) {
                chapters.add(ChapterInfo.builder()
                    .chapterNumber(chapterNum++)
                    .title(title)
                    .url(href)
                    .build());
            }
        }

        log.info("解析到 {} 个章节", chapters.size());
        return chapters;
    }

    @Override
    public String getChapterContent(String chapterUrl) throws IOException {
        log.debug("获取章节内容: {}", chapterUrl);
        Document doc = httpFetcher.fetch(chapterUrl);

        // 笔趣阁常见的内容选择器
        String[] contentSelectors = {
            "#content",
            "#chaptercontent",
            ".chapter-content",
            ".content",
            "article .content"
        };

        for (String selector : contentSelectors) {
            Element content = doc.selectFirst(selector);
            if (content != null) {
                // 移除不需要的元素
                content.select("script, style, .ad, .ads").remove();

                String text = content.html();
                // 将<br>转换为换行
                text = text.replaceAll("<br\\s*/?>", "\n");
                // 移除其他HTML标签
                text = text.replaceAll("<[^>]+>", "");
                // 清理多余空白
                text = text.replaceAll("\\s+", " ").trim();
                // 恢复段落换行
                text = text.replaceAll("(?<=[。！？])\\s*", "\n");

                if (!text.isEmpty()) {
                    log.debug("使用选择器 '{}' 提取到 {} 字内容", selector, text.length());
                    return text;
                }
            }
        }

        log.warn("无法提取章节内容: {}", chapterUrl);
        return "";
    }

    @Override
    public NovelSource getNovelInfo(String novelUrl) throws IOException {
        log.info("获取小说信息: {}", novelUrl);
        Document doc = httpFetcher.fetch(novelUrl);

        // 获取小说名称
        String novelName = "";
        Element titleElement = doc.selectFirst("#info h1");
        if (titleElement != null) {
            novelName = titleElement.text().trim();
        }
        if (novelName.isEmpty()) {
            novelName = doc.title().replace("_笔趣阁", "").replace("-笔趣阁", "").trim();
        }

        // 获取作者
        String author = "";
        Element authorElement = doc.selectFirst("#info p:first-of-type");
        if (authorElement != null) {
            author = authorElement.text().replace("作者：", "").replace("作者:", "").trim();
        }
        if (author.isEmpty()) {
            authorElement = doc.selectFirst("[property=og:novel:author]");
            if (authorElement != null) {
                author = authorElement.attr("content").trim();
            }
        }

        // 获取章节数
        int chapterCount = getCatalog(novelUrl).size();

        return NovelSource.builder()
            .sourceId("biquge_" + extractNovelId(novelUrl))
            .sourceName(getSiteName())
            .novelName(novelName)
            .author(author)
            .chapterCount(chapterCount)
            .url(novelUrl)
            .siteId(getSiteId())
            .build();
    }

    @Override
    public boolean supports(String url) {
        if (url == null) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return SUPPORTED_DOMAINS.stream().anyMatch(lowerUrl::contains) ||
               lowerUrl.contains("biquge") ||
               lowerUrl.contains("bqg");
    }

    /**
     * 从URL中提取小说ID
     */
    private String extractNovelId(String url) {
        // 尝试从URL中提取数字ID
        Pattern pattern = Pattern.compile("/(\\d+)/?$|/(\\d+)\\.html");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        // 使用URL哈希作为备选
        return String.valueOf(url.hashCode());
    }
}
