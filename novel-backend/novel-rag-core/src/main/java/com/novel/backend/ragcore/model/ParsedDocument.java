package com.novel.backend.ragcore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 解析后的文档模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedDocument {

    /**
     * 文档内容
     */
    private String content;

    /**
     * 章节列表
     */
    private List<Chapter> chapters;

    /**
     * 总字数
     */
    private Integer wordCount;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 章节模型
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Chapter {
        /**
         * 章节序号
         */
        private Integer chapterNumber;

        /**
         * 章节标题
         */
        private String title;

        /**
         * 章节内容
         */
        private String content;

        /**
         * 章节字数
         */
        private Integer wordCount;
    }

    /**
     * 计算总字数
     */
    public int calculateWordCount() {
        if (content == null) {
            return 0;
        }
        return content.length();
    }
}
