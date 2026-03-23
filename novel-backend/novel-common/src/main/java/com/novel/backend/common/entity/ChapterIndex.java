package com.novel.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 章节索引实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chapter_index", indexes = {
    @Index(name = "idx_novel_id", columnList = "novel_id"),
    @Index(name = "idx_chapter_number", columnList = "novel_id, chapter_number")
})
public class ChapterIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联小说ID
     */
    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    /**
     * 章节序号
     */
    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    /**
     * 章节标题
     */
    @Column(name = "chapter_title", nullable = false, length = 255)
    private String chapterTitle;

    /**
     * 章节内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
