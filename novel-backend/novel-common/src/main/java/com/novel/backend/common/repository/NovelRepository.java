package com.novel.backend.common.repository;

import com.novel.backend.common.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小说数据访问层
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    /**
     * 根据名称查询
     */
    Optional<Novel> findByName(String name);

    /**
     * 根据名称模糊查询
     */
    List<Novel> findByNameContaining(String name);

    /**
     * 根据作者查询
     */
    List<Novel> findByAuthor(String author);
}
