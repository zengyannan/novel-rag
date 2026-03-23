package com.novel.backend.ragcore.service;

import com.novel.backend.ragcore.model.ParsedDocument;

import java.io.InputStream;

/**
 * 文档解析服务接口
 */
public interface DocumentParser {

    /**
     * 解析文档
     * @param inputStream 文档输入流
     * @param fileType 文件类型 (txt, pdf, docx, epub)
     * @return 解析后的文档
     */
    ParsedDocument parse(InputStream inputStream, String fileType);

    /**
     * 支持的文件类型
     * @return 文件类型数组
     */
    String[] getSupportedTypes();

    /**
     * 是否支持该文件类型
     * @param fileType 文件类型
     * @return 是否支持
     */
    default boolean supports(String fileType) {
        if (fileType == null) {
            return false;
        }
        String type = fileType.toLowerCase();
        for (String supported : getSupportedTypes()) {
            if (supported.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
