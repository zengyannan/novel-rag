package com.novel.backend.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬取结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 小说名称
     */
    private String novelName;

    /**
     * 作者
     */
    private String author;

    /**
     * 来源站点
     */
    private String sourceSite;

    /**
     * 章节列表
     */
    private List<ChapterInfo> chapters;

    /**
     * 总章节数
     */
    private Integer totalChapters;

    /**
     * 创建成功结果
     */
    public static CrawlResult success(String novelName, String author, String sourceSite, List<ChapterInfo> chapters) {
        return CrawlResult.builder()
            .success(true)
            .novelName(novelName)
            .author(author)
            .sourceSite(sourceSite)
            .chapters(chapters)
            .totalChapters(chapters != null ? chapters.size() : 0)
            .build();
    }

    /**
     * 创建失败结果
     */
    public static CrawlResult failure(String errorMessage) {
        return CrawlResult.builder()
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }
}
