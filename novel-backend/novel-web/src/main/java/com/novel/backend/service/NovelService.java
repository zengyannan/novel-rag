package com.novel.backend.service;

import com.novel.backend.common.dto.GenerateRequest;
import com.novel.backend.common.dto.NovelUploadDto;
import com.novel.backend.common.entity.Novel;
import com.novel.backend.common.vo.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 小说服务接口
 */
public interface NovelService {

    /**
     * 上传小说
     * @param file 小说文件
     * @param dto 上传信息
     * @return 小说ID
     */
    Long uploadNovel(MultipartFile file, NovelUploadDto dto);

    /**
     * 获取小说列表
     * @param page 页码
     * @param size 每页数量
     * @return 小说列表
     */
    Result<java.util.List<Novel>> getNovelList(int page, int size);

    /**
     * 获取小说详情
     * @param id 小说ID
     * @return 小说详情
     */
    Novel getNovelById(Long id);

    /**
     * 删除小说
     * @param id 小说ID
     */
    void deleteNovel(Long id);

    /**
     * 索引小说（分块、向量化、存储）
     * @param id 小说ID
     * @return 索引的分块数量
     */
    int indexNovel(Long id);
}
