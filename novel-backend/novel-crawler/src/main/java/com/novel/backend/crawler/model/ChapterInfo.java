package com.novel.backend.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 章节信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterInfo {

    /**
     * 章节序号
     */
    private Integer chapterNumber;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节URL
     */
    private String url;

    /**
     * 章节内容（可选）
     */
    private String content;

    /**
     * 字数
     */
    private Integer wordCount;
}
