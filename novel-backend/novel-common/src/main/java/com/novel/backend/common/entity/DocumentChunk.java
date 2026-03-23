package com.novel.backend.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 文档分块模型
 * 存储在Redis中，不需要JPA注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

    /**
     * 分块ID
     */
    private String id;

    /**
     * 关联小说ID
     */
    private Long novelId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 向量数据
     */
    private float[] embedding;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 计算分块字数
     */
    public int getWordCount() {
        return content != null ? content.length() : 0;
    }
}
