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

/**
 * 通用HTML解析器
 * 用于解析没有特定规则的网站
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class GenericParser implements SiteParser {

    private final HttpFetcher httpFetcher;

    @Override
    public String getSiteId() {
        return "generic";
    }

    @Override
    public String getSiteName() {
        return "通用解析器";
    }

    @Override
    public List<NovelSource> search(String keyword) throws IOException {
        // 通用解析器不支持搜索
        log.warn("通用解析器不支持搜索功能");
        return new ArrayList<>();
    }

    @Override
    public List<ChapterInfo> getCatalog(String novelUrl) throws IOException {
        Document doc = httpFetcher.fetch(novelUrl);
        List<ChapterInfo> chapters = new ArrayList<>();

        // 尝试常见的章节列表选择器
        String[] selectors = {
            "dd a",           // 常见的章节链接
            "li a",           // 列表中的链接
            ".chapter a",     // class包含chapter的链接
            "#list a",        // id为list中的链接
            ".list a",        // class为list中的链接
            "a[href*='chapter']", // href包含chapter的链接
            "a[href*='.html']"    // href包含.html的链接
        };

        for (String selector : selectors) {
            Elements links = doc.select(selector);
            if (!links.isEmpty()) {
                log.info("使用选择器 '{}' 找到 {} 个章节", selector, links.size());
                int chapterNum = 1;
                for (Element link : links) {
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
                if (!chapters.isEmpty()) {
                    break;
                }
            }
        }

        log.info("解析到 {} 个章节", chapters.size());
        return chapters;
    }

    @Override
    public String getChapterContent(String chapterUrl) throws IOException {
        Document doc = httpFetcher.fetch(chapterUrl);

        // 尝试常见的内容选择器
        String[] contentSelectors = {
            "#content",
            ".content",
            "#chaptercontent",
            ".chaptercontent",
            "article",
            ".text-content",
            "#txt",
            ".txt"
        };

        for (String selector : contentSelectors) {
            Element content = doc.selectFirst(selector);
            if (content != null) {
                String text = content.text();
                if (!text.isEmpty()) {
                    log.debug("使用选择器 '{}' 提取到内容", selector);
                    return text;
                }
            }
        }

        // 尝试获取所有段落
        Elements paragraphs = doc.select("p");
        if (!paragraphs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            String content = sb.toString().trim();
            if (!content.isEmpty()) {
                return content;
            }
        }

        log.warn("无法提取章节内容: {}", chapterUrl);
        return "";
    }

    @Override
    public NovelSource getNovelInfo(String novelUrl) throws IOException {
        Document doc = httpFetcher.fetch(novelUrl);

        String title = "";
        String author = "";

        // 尝试获取标题
        Element titleElement = doc.selectFirst("h1");
        if (titleElement != null) {
            title = titleElement.text().trim();
        }
        if (title.isEmpty()) {
            title = doc.title();
        }

        // 尝试获取作者
        String[] authorSelectors = {
            ".author",
            "#author",
            "[itemprop=author]",
            "meta[name=author]"
        };
        for (String selector : authorSelectors) {
            Element authorElement = doc.selectFirst(selector);
            if (authorElement != null) {
                author = authorElement.text().trim();
                if (author.isEmpty() && authorElement.hasAttr("content")) {
                    author = authorElement.attr("content").trim();
                }
                if (!author.isEmpty()) {
                    break;
                }
            }
        }

        int chapterCount = getCatalog(novelUrl).size();

        return NovelSource.builder()
            .sourceId("generic_" + System.currentTimeMillis())
            .sourceName(getSiteName())
            .novelName(title)
            .author(author)
            .chapterCount(chapterCount)
            .url(novelUrl)
            .siteId(getSiteId())
            .build();
    }

    @Override
    public boolean supports(String url) {
        // 通用解析器支持所有HTTP/HTTPS URL
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
