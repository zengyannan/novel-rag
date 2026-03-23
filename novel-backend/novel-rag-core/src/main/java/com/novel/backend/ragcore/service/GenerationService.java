package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.GenerateResult;

/**
 * 文本生成服务接口
 */
public interface GenerationService {

    /**
     * 生成文本
     * @param prompt 提示词
     * @param context 上下文
     * @param maxLength 最大生成长度
     * @param temperature 温度参数
     * @return 生成结果
     */
    GenerateResult generate(String prompt, String context, int maxLength, double temperature);

    /**
     * 生成文本（使用默认参数）
     * @param prompt 提示词
     * @param context 上下文
     * @return 生成结果
     */
    default GenerateResult generate(String prompt, String context) {
        return generate(prompt, context, 2000, 0.8);
    }

    /**
     * 流式生成文本
     * @param prompt 提示词
     * @param context 上下文
     * @param maxLength 最大生成长度
     * @param temperature 温度参数
     * @param listener 流式输出监听器
     */
    void generateStream(String prompt, String context, int maxLength, double temperature,
                        StreamListener listener);

    /**
     * 流式输出监听器
     */
    interface StreamListener {
        /**
         * 接收到新内容
         * @param content 内容片段
         */
        void onContent(String content);

        /**
         * 生成完成
         * @param result 完整结果
         */
        void onComplete(GenerateResult result);

        /**
         * 生成出错
         * @param error 错误信息
         */
        void onError(String error);
    }
}
