package com.order.platform.common.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应封装
 *
 * 功能说明：
 * - 统一分页查询的响应格式
 * - 从 MyBatis-Plus Page 对象快速构建
 * - 支持泛型，适用于任意实体类型
 *
 * 使用示例：
 * <pre>
 * Page page = orderService.page(new Page(1, 10));
 * return Result.success(PageResult.of(page));
 * </pre>
 *
 * @since 1.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult {

    /**
     * 数据列表
     */
    private List records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 从 MyBatis-Plus Page 对象构建分页响应
     *
     * @param page MyBatis-Plus 分页对象
     * @param  实体类型
     * @return 分页响应对象
     */
    public static  PageResult of(Page page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    /**
     * 手动构建分页响应（不依赖 MyBatis-Plus）
     *
     * 使用场景：
     * - 使用自定义分页查询时
     * - 从其他来源获取分页数据时
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param current 当前页码
     * @param size    每页大小
     * @param  实体类型
     * @return 分页响应对象
     */
    public static  PageResult of(List records, long total, long current, long size) {
        // 计算总页数
        long pages = (total + size - 1) / size;
        return new PageResult<>(records, total, current, size, pages);
    }

    /**
     * 空分页响应（用于查询无结果时）
     *
     * @param  实体类型
     * @return 空分页响应对象
     */
    @SuppressWarnings("unchecked")
    public static  PageResult empty() {
        return new PageResult<>(List.of(), 0, 1, 10, 0);
    }
}
