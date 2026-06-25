package com.myk.emotionalHole.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量查询抱抱状态请求DTO
 */
@Data
public class BatchHugStatusRequest {
    
    private int targetType;
    private List<Long> targetIds;
    private String anonymousId;
    
}
