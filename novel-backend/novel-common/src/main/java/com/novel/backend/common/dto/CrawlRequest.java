package com.novel.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬虫请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlRequest {

    /**
     * 小说名称
     */
    @NotBlank(message = "小说名称不能为空")
    private String novelName;

    /**
     * 作者
     */
    private String author;

    /**
     * 来源站点（可选：biquge, qidian等）
     */
    private String sourceSite;

    /**
     * 小说URL（直接抓取时使用）
     */
    private String novelUrl;

    /**
     * 起始章节（从1开始）
     */
    private Integer startChapter;

    /**
     * 结束章节
     */
    private Integer endChapter;

    /**
     * 是否存储正文内容
     */
    private Boolean storeContent;

    /**
     * 指定来源列表（搜索时使用）
     */
    private List<String> sources;
}
