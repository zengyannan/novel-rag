package com.novel.backend.ragcore.service.impl;

import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.model.TextChunk;
import com.novel.backend.ragcore.service.TextSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文本分块服务实现
 * 按段落/章节智能分块
 */
@Slf4j
@Service
public class TextSplitterImpl implements TextSplitter {

    /**
     * 段落分隔符
     */
    private static final String PARAGRAPH_SEPARATOR = "\n\n";

    /**
     * 句子分隔符
     */
    private static final String SENTENCE_SEPARATOR = "。！？";

    @Override
    public List<TextChunk> split(ParsedDocument document, int chunkSize, int overlap) {
        List<TextChunk> allChunks = new ArrayList<>();

        if (document == null || document.getContent() == null) {
            return allChunks;
        }

        // 优先按章节分块
        if (document.getChapters() != null && !document.getChapters().isEmpty()) {
            int globalIndex = 0;
            for (ParsedDocument.Chapter chapter : document.getChapters()) {
                List<TextChunk> chapterChunks = splitChapter(chapter, chunkSize, overlap);
                for (TextChunk chunk : chapterChunks) {
                    chunk.setChunkIndex(globalIndex++);
                    allChunks.add(chunk);
                }
            }
        } else {
            // 没有章节，直接分块
            allChunks = splitText(document.getContent(), chunkSize, overlap);
        }

        log.info("文档分块完成，共 {} 个分块", allChunks.size());
        return allChunks;
    }

    @Override
    public List<TextChunk> splitChapter(ParsedDocument.Chapter chapter, int chunkSize, int overlap) {
        List<TextChunk> chunks = new ArrayList<>();

        if (chapter == null || chapter.getContent() == null) {
            return chunks;
        }

        String content = chapter.getContent();
        chunks = splitText(content, chunkSize, overlap);

        // 设置来源章节
        for (TextChunk chunk : chunks) {
            chunk.setSourceChapter(chapter.getChapterNumber());
        }

        return chunks;
    }

    @Override
    public List<TextChunk> splitText(String text, int chunkSize, int overlap) {
        List<TextChunk> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 验证参数
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("分块大小必须大于0");
        }
        if (overlap < 0) {
            throw new IllegalArgumentException("重叠大小不能为负");
        }
        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("重叠大小必须小于分块大小");
        }

        // 优先按段落分割
        String[] paragraphs = text.split(PARAGRAPH_SEPARATOR);

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            // 如果当前段落加进去不超限，直接加入
            if (currentChunk.length() + paragraph.length() + 2 <= chunkSize) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                // 如果当前段落单独就超限，需要先保存当前chunk，再分割段落
                if (currentChunk.length() > 0) {
                    chunks.add(createChunk(currentChunk.toString(), chunkIndex++));
                    currentChunk = new StringBuilder();
                }

                // 段落超限，按句子分割
                if (paragraph.length() > chunkSize) {
                    List<String> sentences = splitIntoSentences(paragraph, chunkSize);
                    for (String sentence : sentences) {
                        if (sentence.length() > chunkSize) {
                            // 句子还是超限，强制分割
                            chunks.addAll(forceSplit(sentence, chunkSize, overlap, chunkIndex));
                            chunkIndex += (int) Math.ceil((double) sentence.length() / chunkSize);
                        } else if (currentChunk.length() + sentence.length() <= chunkSize) {
                            currentChunk.append(sentence);
                        } else {
                            if (currentChunk.length() > 0) {
                                chunks.add(createChunk(currentChunk.toString(), chunkIndex++));
                            }
                            currentChunk = new StringBuilder(sentence);
                        }
                    }
                } else {
                    currentChunk.append(paragraph);
                }
            }
        }

        // 处理最后一个chunk
        if (currentChunk.length() > 0) {
            chunks.add(createChunk(currentChunk.toString(), chunkIndex));
        }

        return chunks;
    }

    /**
     * 按句子分割文本
     */
    private List<String> splitIntoSentences(String text, int chunkSize) {
        List<String> sentences = new ArrayList<>();
        StringBuilder currentSentence = new StringBuilder();

        for (char c : text.toCharArray()) {
            currentSentence.append(c);
            if (SENTENCE_SEPARATOR.indexOf(c) >= 0) {
                sentences.add(currentSentence.toString());
                currentSentence = new StringBuilder();
            }
        }

        if (currentSentence.length() > 0) {
            sentences.add(currentSentence.toString());
        }

        return sentences;
    }

    /**
     * 强制分割（当句子超长时）
     */
    private List<TextChunk> forceSplit(String text, int chunkSize, int overlap, int startIndex) {
        List<TextChunk> chunks = new ArrayList<>();
        int start = 0;
        int index = startIndex;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunkContent = text.substring(start, end);
            chunks.add(createChunk(chunkContent, index++));
            start = end - overlap;
            if (start < 0) start = 0;
        }

        return chunks;
    }

    /**
     * 创建分块对象
     */
    private TextChunk createChunk(String content, int index) {
        return TextChunk.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .chunkIndex(index)
                .wordCount(content.length())
                .build();
    }
}
