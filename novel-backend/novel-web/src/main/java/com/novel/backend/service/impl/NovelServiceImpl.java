package com.novel.backend.service.impl;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.common.dto.NovelUploadDto;
import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.exception.BusinessException;
import com.novel.backend.common.vo.Result;
import com.novel.backend.ragcore.model.ParsedDocument;
import com.novel.backend.ragcore.service.DocumentParser;
import com.novel.backend.ragcore.service.RagPipeline;
import com.novel.backend.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 小说服务实现
 */
@Slf4j
@Service
public class NovelServiceImpl implements NovelService {

    private final DocumentParser documentParser;
    private final RagPipeline ragPipeline;

    // 简化实现：内存存储（实际应使用数据库）
    private final Map<Long, Novel> novelStore = new HashMap<>();
    private final Map<Long, ParsedDocument> documentStore = new HashMap<>();
    private long idCounter = 1L;

    public NovelServiceImpl(DocumentParser documentParser, RagPipeline ragPipeline) {
        this.documentParser = documentParser;
        this.ragPipeline = ragPipeline;
    }

    @Override
    public Long uploadNovel(MultipartFile file, NovelUploadDto dto) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }

        // 获取文件类型
        String fileType = getFileExtension(originalFilename);
        if (!documentParser.supports(fileType)) {
            throw new BusinessException("不支持的文件类型: " + fileType);
        }

        try {
            // 保存文件（简化实现）
            Long novelId = idCounter++;
            String filePath = saveFile(file, novelId, fileType);

            // 解析文档
            InputStream inputStream = file.getInputStream();
            ParsedDocument document = documentParser.parse(inputStream, fileType);

            // 创建小说实体
            Novel novel = Novel.builder()
                    .id(novelId)
                    .name(dto.getName())
                    .author(dto.getAuthor())
                    .genre(dto.getGenre())
                    .tags(dto.getTags())
                    .description(dto.getDescription())
                    .filePath(filePath)
                    .fileType(fileType)
                    .wordCount(document.getWordCount())
                    .chapterCount(document.getChapters() != null ? document.getChapters().size() : 0)
                    .build();

            // 存储
            novelStore.put(novelId, novel);
            documentStore.put(novelId, document);

            log.info("小说上传成功，ID: {}, 名称: {}", novelId, novel.getName());
            return novelId;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<Novel>> getNovelList(int page, int size) {
        List<Novel> novels = new ArrayList<>(novelStore.values());
        novels.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // 分页
        int start = page * size;
        int end = Math.min(start + size, novels.size());
        List<Novel> pageData = start < novels.size() ? novels.subList(start, end) : new ArrayList<>();

        return Result.success(pageData);
    }

    @Override
    public Novel getNovelById(Long id) {
        if (id == null) {
            throw new BusinessException("小说ID不能为空");
        }
        Novel novel = novelStore.get(id);
        if (novel == null) {
            throw new BusinessException(404, "小说不存在");
        }
        return novel;
    }

    @Override
    public void deleteNovel(Long id) {
        if (id == null) {
            throw new BusinessException("小说ID不能为空");
        }
        Novel novel = novelStore.remove(id);
        if (novel == null) {
            throw new BusinessException(404, "小说不存在");
        }
        documentStore.remove(id);
        log.info("小说删除成功，ID: {}", id);
    }

    @Override
    public int indexNovel(Long id) {
        Novel novel = getNovelById(id);
        ParsedDocument document = documentStore.get(id);

        if (document == null) {
            throw new BusinessException("小说内容未解析");
        }

        int chunkCount = ragPipeline.indexDocument(id, document);
        log.info("小说索引完成，ID: {}, 分块数: {}", id, chunkCount);
        return chunkCount;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file, Long novelId, String fileType) throws IOException {
        String uploadDir = System.getProperty("java.io.tmpdir") + "/novel-rag/uploads/";
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String fileName = "novel_" + novelId + "." + fileType;
        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }
}
