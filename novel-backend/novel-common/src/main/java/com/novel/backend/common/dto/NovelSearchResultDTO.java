package com.novel.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小说搜索结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelSearchResultDTO {

    /**
     * 来源ID
     */
    private String sourceId;

    /**
     * 来源站点名称
     */
    private String sourceName;

    /**
     * 小说名称
     */
    private String novelName;

    /**
     * 作者
     */
    private String author;

    /**
     * 章节数量
     */
    private Integer chapterCount;

    /**
     * 最后更新时间
     */
    private String lastUpdate;

    /**
     * 小说URL
     */
    private String url;
}
