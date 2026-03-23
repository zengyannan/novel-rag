package com.novel.backend.ragcore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 文本分块模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextChunk {

    /**
     * 分块ID
     */
    private String id;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 来源章节序号
     */
    private Integer sourceChapter;

    /**
     * 分块字数
     */
    private Integer wordCount;

    /**
     * 向量数据
     */
    private float[] embedding;

    /**
     * 计算分块字数
     */
    public int calculateWordCount() {
        return content != null ? content.length() : 0;
    }
}
