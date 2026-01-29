package com.company.common.model.dto;

import lombok.Data;

@Data
public class PageQueryDTO<T> {
    private static int PAGE_NUM = 1;
    private static int PAGE_SIZE = 10;

    private int pageNum = PAGE_NUM;
    private int pageSize = PAGE_SIZE;

    private T params;
}
