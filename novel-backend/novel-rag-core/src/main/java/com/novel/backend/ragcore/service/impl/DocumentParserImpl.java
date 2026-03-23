package com.novel.backend.ragcore.service.impl;

import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.service.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档解析服务实现
 * 支持 txt、pdf、docx、epub 格式
 */
@Slf4j
@Service
public class DocumentParserImpl implements DocumentParser {

    /**
     * 章节标题匹配模式
     * 匹配形如：第一章、第1章、Chapter 1 等格式
     */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "^(第[零一二三四五六七八九十百千万\\d]+[章节回]|Chapter\\s*\\d+|CHAPTER\\s*\\d+).*",
            Pattern.MULTILINE
    );

    @Override
    public ParsedDocument parse(InputStream inputStream, String fileType) {
        if (!supports(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }

        String content = extractContent(inputStream, fileType);
        List<ParsedDocument.Chapter> chapters = parseChapters(content);

        return ParsedDocument.builder()
                .content(content)
                .chapters(chapters)
                .wordCount(content.length())
                .build();
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{"txt", "pdf", "docx", "epub"};
    }

    /**
     * 提取文档内容
     */
    private String extractContent(InputStream inputStream, String fileType) {
        return switch (fileType.toLowerCase()) {
            case "txt" -> parseTxt(inputStream);
            case "pdf" -> parsePdf(inputStream);
            case "docx" -> parseDocx(inputStream);
            case "epub" -> parseEpub(inputStream);
            default -> throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        };
    }

    /**
     * 解析TXT文件
     */
    private String parseTxt(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString().trim();
        } catch (Exception e) {
            log.error("解析TXT文件失败", e);
            throw new RuntimeException("解析TXT文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) {
        // PDF解析逻辑，使用 PDFBox
        try {
            org.apache.pdfbox.Loader.loadPDF(inputStream.readAllBytes())
                    .getPages()
                    .forEach(page -> {
                        // 简化实现，实际需要提取文本
                    });
            // 简化版本，返回提示信息
            log.warn("PDF解析功能需要完整的实现");
            return "PDF内容解析需要完整实现";
        } catch (Exception e) {
            log.error("解析PDF文件失败", e);
            throw new RuntimeException("解析PDF文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析DOCX文件
     */
    private String parseDocx(InputStream inputStream) {
        // DOCX解析逻辑，使用 Apache POI
        try {
            org.apache.poi.xwpf.usermodel.XWPFDocument document =
                    new org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream);
            StringBuilder content = new StringBuilder();
            for (org.apache.poi.xwpf.usermodel.XWPFParagraph para : document.getParagraphs()) {
                content.append(para.getText()).append("\n");
            }
            document.close();
            return content.toString().trim();
        } catch (Exception e) {
            log.error("解析DOCX文件失败", e);
            throw new RuntimeException("解析DOCX文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析EPUB文件
     */
    private String parseEpub(InputStream inputStream) {
        // EPUB解析逻辑
        log.warn("EPUB解析功能需要完整的实现");
        return "EPUB内容解析需要完整实现";
    }

    /**
     * 解析章节
     */
    private List<ParsedDocument.Chapter> parseChapters(String content) {
        List<ParsedDocument.Chapter> chapters = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chapters;
        }

        Matcher matcher = CHAPTER_PATTERN.matcher(content);
        List<Integer> chapterStarts = new ArrayList<>();
        List<String> chapterTitles = new ArrayList<>();

        // 找到所有章节起始位置
        while (matcher.find()) {
            chapterStarts.add(matcher.start());
            chapterTitles.add(matcher.group().trim());
        }

        // 如果没有找到章节，将整个内容作为一个章节
        if (chapterStarts.isEmpty()) {
            chapters.add(ParsedDocument.Chapter.builder()
                    .chapterNumber(1)
                    .title("第一章")
                    .content(content)
                    .wordCount(content.length())
                    .build());
            return chapters;
        }

        // 提取每个章节的内容
        for (int i = 0; i < chapterStarts.size(); i++) {
            int start = chapterStarts.get(i);
            int end = (i + 1 < chapterStarts.size()) ? chapterStarts.get(i + 1) : content.length();
            String chapterContent = content.substring(start, end).trim();

            chapters.add(ParsedDocument.Chapter.builder()
                    .chapterNumber(i + 1)
                    .title(chapterTitles.get(i))
                    .content(chapterContent)
                    .wordCount(chapterContent.length())
                    .build());
        }

        return chapters;
    }
}
