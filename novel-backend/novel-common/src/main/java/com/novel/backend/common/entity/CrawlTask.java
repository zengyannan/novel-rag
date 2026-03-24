package com.novel.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 爬虫任务实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "crawl_task", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_novel_id", columnList = "novel_id")
})
public class CrawlTask {

    @Id
    @Column(length = 50)
    private String id;

    /**
     * 关联小说ID
     */
    @Column(name = "novel_id")
    private Long novelId;

    /**
     * 小说名称
     */
    @Column(name = "novel_name", nullable = false, length = 255)
    private String novelName;

    /**
     * 作者
     */
    @Column(length = 100)
    private String author;

    /**
     * 来源站点
     */
    @Column(name = "source_site", nullable = false, length = 50)
    private String sourceSite;

    /**
     * 来源URL
     */
    @Column(name = "source_url", nullable = false, length = 1024)
    private String sourceUrl;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    /**
     * 总章节数
     */
    @Column(name = "total_chapters")
    private Integer totalChapters;

    /**
     * 已抓取章节数
     */
    @Column(name = "crawled_chapters")
    private Integer crawledChapters;

    /**
     * 起始章节
     */
    @Column(name = "start_chapter")
    private Integer startChapter;

    /**
     * 结束章节
     */
    @Column(name = "end_chapter")
    private Integer endChapter;

    /**
     * 是否存储正文
     */
    @Column(name = "store_content")
    private Boolean storeContent;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING,
        RUNNING,
        PAUSED,
        COMPLETED,
        FAILED
    }
}
