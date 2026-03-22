package com.novel.backend.controller;

import com.novel.backend.common.vo.HealthInfoVO;
import com.novel.backend.common.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<HealthInfoVO> health() {
        HealthInfoVO data = new HealthInfoVO("UP");
        return Result.success("小说RAG系统运行正常", data);
    }

}
