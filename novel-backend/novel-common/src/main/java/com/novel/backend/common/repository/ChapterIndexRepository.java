package com.novel.backend.common.repository;

import com.novel.backend.common.entity.ChapterIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节索引数据访问层
 */
@Repository
public interface ChapterIndexRepository extends JpaRepository<ChapterIndex, Long> {

    /**
     * 根据小说ID查询章节列表
     */
    List<ChapterIndex> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    /**
     * 根据小说ID和章节号查询
     */
    Optional<ChapterIndex> findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    /**
     * 根据小说ID统计章节数
     */
    long countByNovelId(Long novelId);

    /**
     * 根据小说ID删除所有章节
     */
    void deleteByNovelId(Long novelId);
}
