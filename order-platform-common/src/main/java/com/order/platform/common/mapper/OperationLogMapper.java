package com.order.platform.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.order.platform.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * 功能说明：
 * - 继承 MyBatis-Plus 的 BaseMapper，自动拥有 CRUD 方法
 * - 无需手写 SQL，简化开发
 *
 * @since 1.0.0
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    // 自动继承以下方法：
    // - insert(OperationLog entity)          插入
    // - deleteById(Serializable id)          根据ID删除
    // - updateById(OperationLog entity)      根据ID更新
    // - selectById(Serializable id)          根据ID查询
    // - selectList(Wrapper<OperationLog>)     条件查询
    // - selectPage(Page, Wrapper)            分页查询
}
