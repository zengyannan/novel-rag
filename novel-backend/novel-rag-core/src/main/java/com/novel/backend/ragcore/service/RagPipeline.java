package com.novel.backend.ragcore.service;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;
import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;

import java.util.List;

/**
 * RAG 生成流程服务接口
 */
public interface RagPipeline {

    /**
     * 执行 RAG 生成流程
     * 1. 解析文档
     * 2. 文本分块
     * 3. 向量化
     * 4. 存储向量
     * 5. 检索相关内容
     * 6. 生成文本
     *
     * @param documentContent 文档内容
     * @param request 生成请求
     * @return 生成结果
     */
    GenerateResult execute(String documentContent, GenerateRequest request);

    /**
     * 执行 RAG 生成流程（使用已存在的小说）
     * @param novelId 小说ID
     * @param request 生成请求
     * @return 生成结果
     */
    GenerateResult executeWithNovel(Long novelId, GenerateRequest request);

    /**
     * 索引文档
     * 将文档分块、向量化并存储
     *
     * @param novelId 小说ID
     * @param document 解析后的文档
     * @return 索引的分块数量
     */
    int indexDocument(Long novelId, ParsedDocument document);

    /**
     * 检索相关内容
     * @param novelId 小说ID
     * @param query 查询文本
     * @param topK 返回数量
     * @return 相关内容列表
     */
    List<TextChunk> retrieveRelevantContent(Long novelId, String query, int topK);

    /**
     * 构建上下文
     * 将检索到的内容整合成上下文
     *
     * @param chunks 相关内容分块
     * @return 上下文字符串
     */
    String buildContext(List<TextChunk> chunks);
}
