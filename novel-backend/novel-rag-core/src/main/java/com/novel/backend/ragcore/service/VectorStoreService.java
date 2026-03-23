package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.TextChunk;

import java.util.List;

/**
 * 向量存储服务接口
 */
public interface VectorStoreService {

    /**
     * 存储文档分块及其向量
     * @param chunk 文档分块
     * @param embedding 向量数据
     */
    void store(TextChunk chunk, float[] embedding);

    /**
     * 批量存储文档分块及其向量
     * @param chunks 文档分块列表
     * @param embeddings 向量数据列表
     */
    void storeBatch(List<TextChunk> chunks, List<float[]> embeddings);

    /**
     * 相似度检索
     * @param query 查询文本
     * @param topK 返回最相似的K个结果
     * @return 相似的文档分块列表
     */
    List<TextChunk> search(String query, int topK);

    /**
     * 向量相似度检索
     * @param embedding 查询向量
     * @param topK 返回最相似的K个结果
     * @return 相似的文档分块列表
     */
    List<TextChunk> searchByVector(float[] embedding, int topK);

    /**
     * 根据小说ID删除所有相关向量
     * @param novelId 小说ID
     */
    void deleteByNovelId(Long novelId);

    /**
     * 检查向量索引是否存在
     * @return 是否存在
     */
    boolean indexExists();

    /**
     * 创建向量索引
     */
    void createIndex();
}
