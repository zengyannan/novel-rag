package com.novel.backend.service;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.ragcore.model.GenerateResult;

/**
 * 生成 API 服务接口
 */
public interface GenerationApiService {

    /**
     * 生成章节
     * @param request 生成请求
     * @return 生成结果
     */
    GenerateResult generateChapter(GenerateRequest request);

    /**
     * 流式生成章节
     * @param request 生成请求
     * @param callback 回调
     */
    void generateChapterStream(GenerateRequest request, StreamCallback callback);

    /**
     * 流式回调接口
     */
    interface StreamCallback {
        /**
         * 接收到内容
         */
        void onContent(String content);

        /**
         * 生成完成
         */
        void onComplete(GenerateResult result);

        /**
         * 生成出错
         */
        void onError(String error);
    }
}
