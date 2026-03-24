package com.novel.backend.controller;

import com.novel.backend.common.dto.CrawlRequest;
import com.novel.backend.common.dto.CrawlResultDTO;
import com.novel.backend.common.dto.NovelSearchResultDTO;
import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.vo.Result;
import com.novel.backend.crawler.model.ChapterInfo;
import com.novel.backend.crawler.model.CrawlResult;
import com.novel.backend.crawler.model.NovelSource;
import com.novel.backend.crawler.service.ChapterIndexService;
import com.novel.backend.crawler.service.CrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 爬虫控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Tag(name = "爬虫接口", description = "小说爬虫相关API")
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final ChapterIndexService chapterIndexService;

    @Operation(summary = "搜索小说", description = "根据小说名称搜索可用的来源")
    @PostMapping("/search")
    public Result<List<NovelSearchResultDTO>> searchNovel(@Valid @RequestBody CrawlRequest request) {
        log.info("搜索小说: {}", request.getNovelName());

        List<NovelSource> sources;
        if (request.getSources() != null && !request.getSources().isEmpty()) {
            // 指定站点搜索
            sources = crawlerService.searchNovel(request.getNovelName(), request.getSources().get(0));
        } else {
            // 默认搜索
            sources = crawlerService.searchNovel(request.getNovelName());
        }

        List<NovelSearchResultDTO> results = sources.stream()
            .map(this::toSearchResultDTO)
            .collect(Collectors.toList());

        return Result.success(results);
    }

    @Operation(summary = "获取小说目录", description = "获取小说的章节目录")
    @GetMapping("/catalog")
    public Result<List<CrawlResultDTO.ChapterDTO>> getCatalog(
        @Parameter(description = "小说URL") @RequestParam String url) {
        log.info("获取小说目录: {}", url);

        List<ChapterInfo> chapters = crawlerService.getCatalog(url);

        List<CrawlResultDTO.ChapterDTO> chapterDTOs = chapters.stream()
            .map(this::toChapterDTO)
            .collect(Collectors.toList());

        return Result.success(chapterDTOs);
    }

    @Operation(summary = "获取小说信息", description = "获取小说的基本信息")
    @GetMapping("/info")
    public Result<NovelSearchResultDTO> getNovelInfo(
        @Parameter(description = "小说URL") @RequestParam String url) {
        log.info("获取小说信息: {}", url);

        NovelSource source = crawlerService.getNovelInfo(url);
        if (source == null) {
            return Result.fail("获取小说信息失败");
        }

        return Result.success(toSearchResultDTO(source));
    }

    @Operation(summary = "获取章节内容", description = "获取单个章节的内容")
    @GetMapping("/chapter")
    public Result<String> getChapterContent(
        @Parameter(description = "章节URL") @RequestParam String url) {
        log.info("获取章节内容: {}", url);

        String content = crawlerService.getChapterContent(url);
        return Result.success(content);
    }

    @Operation(summary = "抓取小说", description = "抓取小说并保存到本地")
    @PostMapping("/start")
    public Result<CrawlResultDTO> crawlNovel(@Valid @RequestBody CrawlRequest request) {
        log.info("开始抓取小说: {}", request.getNovelName());

        // 确定小说URL
        String novelUrl = request.getNovelUrl();
        if (novelUrl == null || novelUrl.isEmpty()) {
            // 如果没有提供URL，先搜索
            List<NovelSource> sources = crawlerService.searchNovel(request.getNovelName());
            if (sources.isEmpty()) {
                return Result.fail("未找到小说: " + request.getNovelName());
            }
            novelUrl = sources.get(0).getUrl();
        }

        // 执行抓取
        boolean storeContent = request.getStoreContent() != null && request.getStoreContent();
        CrawlResult result = crawlerService.crawlNovel(
            novelUrl,
            request.getStartChapter(),
            request.getEndChapter(),
            storeContent
        );

        if (!result.getSuccess()) {
            return Result.fail(result.getErrorMessage());
        }

        // 保存到数据库
        Novel novel = chapterIndexService.saveCrawlResult(result);

        // 构建返回结果
        CrawlResultDTO resultDTO = CrawlResultDTO.builder()
            .success(true)
            .novelId(novel != null ? novel.getId() : null)
            .novelName(result.getNovelName())
            .author(result.getAuthor())
            .sourceSite(result.getSourceSite())
            .totalChapters(result.getTotalChapters())
            .crawledChapters(result.getChapters() != null ? result.getChapters().size() : 0)
            .chapters(result.getChapters() != null ?
                result.getChapters().stream().map(this::toChapterDTO).collect(Collectors.toList()) :
                null)
            .build();

        return Result.success(resultDTO);
    }

    @Operation(summary = "抓取指定章节", description = "抓取小说的指定章节范围")
    @PostMapping("/crawl-range")
    public Result<CrawlResultDTO> crawlChapterRange(@Valid @RequestBody CrawlRequest request) {
        log.info("抓取章节范围: {} - {}", request.getStartChapter(), request.getEndChapter());

        if (request.getNovelUrl() == null || request.getNovelUrl().isEmpty()) {
            return Result.fail("请提供小说URL");
        }

        boolean storeContent = request.getStoreContent() != null && request.getStoreContent();
        CrawlResult result = crawlerService.crawlNovel(
            request.getNovelUrl(),
            request.getStartChapter(),
            request.getEndChapter(),
            storeContent
        );

        if (!result.getSuccess()) {
            return Result.fail(result.getErrorMessage());
        }

        // 保存到数据库
        Novel novel = chapterIndexService.saveCrawlResult(result);

        CrawlResultDTO resultDTO = CrawlResultDTO.builder()
            .success(true)
            .novelId(novel != null ? novel.getId() : null)
            .novelName(result.getNovelName())
            .author(result.getAuthor())
            .sourceSite(result.getSourceSite())
            .totalChapters(result.getTotalChapters())
            .crawledChapters(result.getChapters() != null ? result.getChapters().size() : 0)
            .build();

        return Result.success(resultDTO);
    }

    /**
     * 转换为搜索结果DTO
     */
    private NovelSearchResultDTO toSearchResultDTO(NovelSource source) {
        return NovelSearchResultDTO.builder()
            .sourceId(source.getSourceId())
            .sourceName(source.getSourceName())
            .novelName(source.getNovelName())
            .author(source.getAuthor())
            .chapterCount(source.getChapterCount())
            .lastUpdate(source.getLastUpdate())
            .url(source.getUrl())
            .build();
    }

    /**
     * 转换为章节DTO
     */
    private CrawlResultDTO.ChapterDTO toChapterDTO(ChapterInfo chapter) {
        return CrawlResultDTO.ChapterDTO.builder()
            .chapterNumber(chapter.getChapterNumber())
            .title(chapter.getTitle())
            .url(chapter.getUrl())
            .wordCount(chapter.getWordCount())
            .build();
    }
}
