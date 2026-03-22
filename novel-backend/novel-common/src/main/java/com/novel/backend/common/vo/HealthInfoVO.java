package com.novel.backend.common.vo;

import lombok.Data;

/**
 * 健康检查信息
 */
@Data
public class HealthInfoVO {

    /**
     * 服务状态
     */
    private String status;

    public HealthInfoVO() {
    }

    public HealthInfoVO(String status) {
        this.status = status;
    }

}
