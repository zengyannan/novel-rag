package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;

import java.util.List;

/**
 * 文本分块服务接口
 */
public interface TextSplitter {

    /**
     * 将文档分块
     * @param document 解析后的文档
     * @param chunkSize 分块大小（字符数）
     * @param overlap 重叠字符数
     * @return 分块列表
     */
    List<TextChunk> split(ParsedDocument document, int chunkSize, int overlap);

    /**
     * 将文档分块（使用默认参数）
     * @param document 解析后的文档
     * @return 分块列表
     */
    default List<TextChunk> split(ParsedDocument document) {
        return split(document, 500, 50);
    }

    /**
     * 将单个章节分块
     * @param chapter 章节
     * @param chunkSize 分块大小
     * @param overlap 重叠字符数
     * @return 分块列表
     */
    List<TextChunk> splitChapter(ParsedDocument.Chapter chapter, int chunkSize, int overlap);

    /**
     * 将文本分块
     * @param text 文本内容
     * @param chunkSize 分块大小
     * @param overlap 重叠字符数
     * @return 分块列表
     */
    List<TextChunk> splitText(String text, int chunkSize, int overlap);
}
