package com.novel.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 小说上传请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelUploadDto {

    /**
     * 小说名称
     */
    @NotBlank(message = "小说名称不能为空")
    @Size(max = 255, message = "小说名称不能超过255个字符")
    private String name;

    /**
     * 作者
     */
    @Size(max = 100, message = "作者名称不能超过100个字符")
    private String author;

    /**
     * 小说类型
     */
    @Size(max = 50, message = "类型不能超过50个字符")
    private String genre;

    /**
     * 标签
     */
    @Size(max = 500, message = "标签不能超过500个字符")
    private String tags;

    /**
     * 简介
     */
    private String description;
}
