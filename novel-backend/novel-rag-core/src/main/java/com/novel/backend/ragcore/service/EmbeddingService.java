package com.novel.backend.ragcore.service;

import java.util.List;

/**
 * 文本向量化服务接口
 */
public interface EmbeddingService {

    /**
     * 将文本转换为向量
     * @param text 文本内容
     * @return 向量数据
     */
    float[] embed(String text);

    /**
     * 批量将文本转换为向量
     * @param texts 文本列表
     * @return 向量列表
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * 获取向量维度
     * @return 向量维度
     */
    int getDimension();

    /**
     * 计算两个向量的余弦相似度
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度（-1到1之间）
     */
    double cosineSimilarity(float[] vector1, float[] vector2);
}
