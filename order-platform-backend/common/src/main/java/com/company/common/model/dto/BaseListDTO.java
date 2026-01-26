package com.company.common.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

import com.company.common.enums.model.SortDirection;
import com.company.common.enums.model.dto.FilterTimeDTO;
@Data
public class BaseListDTO {
    /**
     * 排序字段
     */
    private SortDTO sort;

    /**
     * 时间筛选
     */
    private FilterTimeDTO filterTime;

    /**
     * 添加时间筛选
     * @param field 时间字段
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    public void addFilterTime(String field, LocalDateTime startTime, LocalDateTime endTime) {
        filterTime = new FilterTimeDTO(field, startTime, endTime);
    }

    /**
     * 添加排序字段
     * @param field 排序字段
     * @param direction 排序方向
     */
    public void addSort(String field, SortDirection direction) {
        sort = new SortDTO(field, direction);
    }

    /**
     * 添加排序字段(默认升序)
     * @param field 排序字段
     */
    public void addSort(String field) {
        sort = new SortDTO(field, SortDirection.ASC);
    }
}
