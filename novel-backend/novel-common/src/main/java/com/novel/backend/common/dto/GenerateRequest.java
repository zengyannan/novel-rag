package com.novel.backend.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 章节生成请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateRequest {

    /**
     * 小说ID
     */
    @NotNull(message = "小说ID不能为空")
    private Long novelId;

    /**
     * 生成提示词
     */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 2000, message = "提示词不能超过2000个字符")
    private String prompt;

    /**
     * 章节标题
     */
    @Size(max = 255, message = "章节标题不能超过255个字符")
    private String chapterTitle;

    /**
     * 风格
     */
    @Size(max = 50, message = "风格不能超过50个字符")
    private String style;

    /**
     * 最大生成长度
     */
    @Min(value = 100, message = "最小生成长度为100")
    @Max(value = 8000, message = "最大生成长度为8000")
    @Builder.Default
    private Integer maxLength = 2000;

    /**
     * 温度参数
     */
    @Min(value = 0, message = "温度参数最小为0")
    @Max(value = 2, message = "温度参数最大为2")
    @Builder.Default
    private Double temperature = 0.8;
}
