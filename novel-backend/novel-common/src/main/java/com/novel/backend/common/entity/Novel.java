package com.novel.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 小说实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "novel")
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 小说名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 作者
     */
    @Column(length = 100)
    private String author;

    /**
     * 小说类型
     */
    @Column(length = 50)
    private String genre;

    /**
     * 标签
     */
    @Column(length = 500)
    private String tags;

    /**
     * 文件路径
     */
    @Column(length = 500)
    private String filePath;

    /**
     * 文件类型
     */
    @Column(length = 20)
    private String fileType;

    /**
     * 总字数
     */
    private Integer wordCount;

    /**
     * 章节数
     */
    private Integer chapterCount;

    /**
     * 简介
     */
    @Column(columnDefinition = "TEXT")
    private String description;

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
}
