package com.novel.backend.crawler.parser;

import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.NovelSource;

import java.io.IOException;
import java.util.List;

/**
 * 站点解析器接口
 */
public interface SiteParser {

    /**
     * 获取站点ID
     */
    String getSiteId();

    /**
     * 获取站点名称
     */
    String getSiteName();

    /**
     * 搜索小说
     *
     * @param keyword 搜索关键词
     * @return 小说来源列表
     */
    List<NovelSource> search(String keyword) throws IOException;

    /**
     * 获取小说目录
     *
     * @param novelUrl 小说主页URL
     * @return 章节列表
     */
    List<ChapterInfo> getCatalog(String novelUrl) throws IOException;

    /**
     * 获取章节内容
     *
     * @param chapterUrl 章节URL
     * @return 章节内容
     */
    String getChapterContent(String chapterUrl) throws IOException;

    /**
     * 获取小说信息
     *
     * @param novelUrl 小说主页URL
     * @return 小说来源信息
     */
    NovelSource getNovelInfo(String novelUrl) throws IOException;

    /**
     * 是否支持该URL
     *
     * @param url URL
     * @return 是否支持
     */
    boolean supports(String url);
}
