package com.novel.backend.controller;

import com.novel.backend.common.dto.NovelUploadDto;
import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.vo.Result;
import com.novel.backend.service.NovelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 小说管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
@Tag(name = "小说管理", description = "小说上传、查询、删除等接口")
public class NovelController {

    private final NovelService novelService;

    /**
     * 上传小说
     */
    @PostMapping("/upload")
    @Operation(summary = "上传小说", description = "上传小说文件并解析")
    public Result<Map<String, Object>> uploadNovel(
            @Parameter(description = "小说文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "小说名称") @RequestParam("name") String name,
            @Parameter(description = "作者") @RequestParam(value = "author", required = false) String author,
            @Parameter(description = "类型") @RequestParam(value = "genre", required = false) String genre,
            @Parameter(description = "标签") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "简介") @RequestParam(value = "description", required = false) String description) {

        NovelUploadDto dto = NovelUploadDto.builder()
                .name(name)
                .author(author)
                .genre(genre)
                .tags(tags)
                .description(description)
                .build();

        Long novelId = novelService.uploadNovel(file, dto);

        return Result.success(Map.of(
                "novelId", novelId,
                "message", "上传成功"
        ));
    }

    /**
     * 获取小说列表
     */
    @GetMapping
    @Operation(summary = "获取小说列表", description = "分页获取小说列表")
    public Result<List<Novel>> getNovelList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        return novelService.getNovelList(page, size);
    }

    /**
     * 获取小说详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取小说详情", description = "根据ID获取小说详情")
    public Result<Novel> getNovelById(
            @Parameter(description = "小说ID") @PathVariable Long id) {

        Novel novel = novelService.getNovelById(id);
        return Result.success(novel);
    }

    /**
     * 删除小说
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除小说", description = "根据ID删除小说")
    public Result<Void> deleteNovel(
            @Parameter(description = "小说ID") @PathVariable Long id) {

        novelService.deleteNovel(id);
        return Result.success("删除成功", null);
    }

    /**
     * 索引小说
     */
    @PostMapping("/{id}/index")
    @Operation(summary = "索引小说", description = "对小说进行分块、向量化、存储")
    public Result<Map<String, Object>> indexNovel(
            @Parameter(description = "小说ID") @PathVariable Long id) {

        int chunkCount = novelService.indexNovel(id);
        return Result.success(Map.of(
                "chunkCount", chunkCount,
                "message", "索引完成"
        ));
    }
}
