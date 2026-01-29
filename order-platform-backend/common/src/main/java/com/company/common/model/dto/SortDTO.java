package com.company.common.model.dto;

import com.company.common.enums.model.SortDirection;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortDTO {
    
    /**
     * 排序字段
     */
    @NotBlank(message = "字段不能为空")
    private String field;

    /**
     * 排序方向
     */
    private SortDirection direction;
}
