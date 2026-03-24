package com.novel.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬虫结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlResultDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 小说ID
     */
    private Long novelId;

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
     * 总章节数
     */
    private Integer totalChapters;

    /**
     * 已抓取章节数
     */
    private Integer crawledChapters;

    /**
     * 章节列表
     */
    private List<ChapterDTO> chapters;

    /**
     * 章节DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChapterDTO {
        private Integer chapterNumber;
        private String title;
        private String url;
        private Integer wordCount;
    }
}
