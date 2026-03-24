package com.novel.backend.crawler.service;

import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.CrawlResult;
import com.novel.backend.common.entity.ChapterIndex;
import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.repository.ChapterIndexRepository;
import com.novel.backend.common.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节索引存储服务
 * 保存章节索引到数据库，保存内容到本地文件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterIndexService {

    private final ChapterIndexRepository chapterIndexRepository;
    private final NovelRepository novelRepository;

    @Value("${crawler.storage.base-path:novel-txt}")
    private String basePath;

    /**
     * 保存爬取结果
     *
     * @param result 爬取结果
     * @return 创建的小说实体
     */
    @Transactional
    public Novel saveCrawlResult(CrawlResult result) {
        if (!result.getSuccess() || result.getChapters() == null || result.getChapters().isEmpty()) {
            log.warn("爬取结果无效，无法保存");
            return null;
        }

        // 创建或更新小说实体
        Novel novel = Novel.builder()
            .name(result.getNovelName())
            .author(result.getAuthor())
            .chapterCount(result.getChapters().size())
            .wordCount(0)
            .fileType("crawler")
            .build();

        novel = novelRepository.save(novel);
        log.info("创建小说记录: {} (ID: {})", novel.getName(), novel.getId());

        // 创建小说目录
        Path novelDir = createNovelDirectory(novel.getId(), novel.getName());

        // 保存章节
        List<ChapterIndex> chapterIndices = new ArrayList<>();
        int totalWords = 0;

        for (ChapterInfo chapterInfo : result.getChapters()) {
            // 保存章节内容到文件
            String contentPath = null;
            if (chapterInfo.getContent() != null && !chapterInfo.getContent().isEmpty()) {
                contentPath = saveChapterFile(novelDir, chapterInfo);
            }

            // 创建章节索引
            ChapterIndex chapterIndex = ChapterIndex.builder()
                .novelId(novel.getId())
                .chapterNumber(chapterInfo.getChapterNumber())
                .chapterTitle(chapterInfo.getTitle())
                .sourceUrl(chapterInfo.getUrl())
                .sourceSite(result.getSourceSite())
                .wordCount(chapterInfo.getWordCount())
                .crawlTime(LocalDateTime.now())
                .build();

            // 如果存储内容，将内容也存入数据库
            if (contentPath != null) {
                chapterIndex.setContent(chapterInfo.getContent());
            }

            chapterIndices.add(chapterIndex);

            if (chapterInfo.getWordCount() != null) {
                totalWords += chapterInfo.getWordCount();
            }
        }

        // 批量保存章节索引
        chapterIndexRepository.saveAll(chapterIndices);

        // 更新小说总字数
        novel.setWordCount(totalWords);
        novelRepository.save(novel);

        log.info("保存完成: {} 章, {} 字", chapterIndices.size(), totalWords);
        return novel;
    }

    /**
     * 保存单个章节
     *
     * @param novelId     小说ID
     * @param chapterInfo 章节信息
     * @return 章节索引实体
     */
    @Transactional
    public ChapterIndex saveChapter(Long novelId, ChapterInfo chapterInfo, String sourceSite) {
        // 检查是否已存在
        ChapterIndex existing = chapterIndexRepository
            .findByNovelIdAndChapterNumber(novelId, chapterInfo.getChapterNumber())
            .orElse(null);

        if (existing != null) {
            log.debug("章节已存在: {} - {}", novelId, chapterInfo.getChapterNumber());
            return existing;
        }

        // 创建小说目录
        Path novelDir = createNovelDirectory(novelId, "novel_" + novelId);

        // 保存章节文件
        String contentPath = null;
        if (chapterInfo.getContent() != null && !chapterInfo.getContent().isEmpty()) {
            contentPath = saveChapterFile(novelDir, chapterInfo);
        }

        ChapterIndex chapterIndex = ChapterIndex.builder()
            .novelId(novelId)
            .chapterNumber(chapterInfo.getChapterNumber())
            .chapterTitle(chapterInfo.getTitle())
            .sourceUrl(chapterInfo.getUrl())
            .sourceSite(sourceSite)
            .content(chapterInfo.getContent())
            .wordCount(chapterInfo.getWordCount())
            .crawlTime(LocalDateTime.now())
            .build();

        return chapterIndexRepository.save(chapterIndex);
    }

    /**
     * 创建小说目录
     */
    private Path createNovelDirectory(Long novelId, String novelName) {
        // 清理小说名称中的非法字符
        String safeName = novelName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String dirName = novelId + "_" + safeName;

        Path novelDir = Paths.get(basePath, dirName);
        try {
            if (!Files.exists(novelDir)) {
                Files.createDirectories(novelDir);
                log.info("创建小说目录: {}", novelDir);
            }
        } catch (IOException e) {
            log.error("创建目录失败: {}", novelDir, e);
            throw new RuntimeException("无法创建小说目录: " + e.getMessage());
        }

        return novelDir;
    }

    /**
     * 保存章节内容到文件
     *
     * @return 文件路径
     */
    private String saveChapterFile(Path novelDir, ChapterInfo chapterInfo) {
        String fileName = String.format("%04d_%s.txt",
            chapterInfo.getChapterNumber(),
            sanitizeFileName(chapterInfo.getTitle()));

        Path filePath = novelDir.resolve(fileName);

        try {
            String content = formatChapterContent(chapterInfo);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.debug("保存章节文件: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("保存章节文件失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 格式化章节内容
     */
    private String formatChapterContent(ChapterInfo chapterInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(chapterInfo.getChapterNumber()).append("章 ")
            .append(chapterInfo.getTitle()).append("\n\n");
        sb.append(chapterInfo.getContent());
        return sb.toString();
    }

    /**
     * 清理文件名
     */
    private String sanitizeFileName(String name) {
        if (name == null) {
            return "untitled";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
            .replaceAll("\\s+", "_")
            .substring(0, Math.min(name.length(), 50));
    }

    /**
     * 获取小说的所有章节
     */
    public List<ChapterIndex> getChaptersByNovelId(Long novelId) {
        return chapterIndexRepository.findByNovelIdOrderByChapterNumberAsc(novelId);
    }

    /**
     * 获取单个章节
     */
    public ChapterIndex getChapter(Long novelId, Integer chapterNumber) {
        return chapterIndexRepository.findByNovelIdAndChapterNumber(novelId, chapterNumber)
            .orElse(null);
    }

    /**
     * 删除小说的所有章节
     */
    @Transactional
    public void deleteByNovelId(Long novelId) {
        chapterIndexRepository.deleteByNovelId(novelId);
        log.info("删除小说 {} 的所有章节", novelId);
    }
}
