package com.novel.backend.ragcore.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 文本生成结果模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateResult {

    /**
     * 生成的文本内容
     */
    private String content;

    /**
     * 章节标题
     */
    private String chapterTitle;

    /**
     * 字数统计
     */
    private Integer wordCount;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 提示词token数
     */
    private Integer promptTokens;

    /**
     * 生成token数
     */
    private Integer completionTokens;

    /**
     * 总token数
     */
    private Integer totalTokens;

    /**
     * 计算字数
     */
    public int calculateWordCount() {
        return content != null ? content.length() : 0;
    }
}
