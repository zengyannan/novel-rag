package com.novel.backend.common.repository;

import com.novel.backend.common.entity.CrawlTask;
import com.novel.backend.common.entity.CrawlTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 爬虫任务数据访问层
 */
@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, String> {

    /**
     * 根据状态查询任务
     */
    List<CrawlTask> findByStatus(TaskStatus status);

    /**
     * 根据小说ID查询任务
     */
    List<CrawlTask> findByNovelId(Long novelId);
}
