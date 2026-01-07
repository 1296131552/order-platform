package com.order.platform.common.service;

import com.order.platform.common.dto.OperationLogDTO;

/**
 * 操作日志服务接口
 *
 * 功能说明：
 * - 提供操作日志的异步保存功能
 * - 混合存储：核心信息存 MySQL，详细快照存对象存储
 *
 * @since 1.0.0
 */
public interface OperationLogService {

    /**
     * 异步保存操作日志
     *
     * 说明：
     * - 使用独立线程池异步执行，不影响业务性能
     * - 核心信息保存到 MySQL（t_operation_log 表）
     * - 详细快照保存到对象存储（OSS/MinIO）
     *
     * @param log 操作日志DTO
     */
    void saveAsync(OperationLogDTO log);

    /**
     * 保存数据快照到对象存储
     *
     * 说明：
     * - 快照数据包含 before_data 和 after_data
     * - 文件路径：logs/operation/{year}/{month}/{logId}.json
     * - 文件内容：{"before": {...}, "after": {...}}
     *
     * @param logId 日志ID
     * @param beforeData 操作前数据（JSON字符串）
     * @param afterData 操作后数据（JSON字符串）
     * @return 对象存储的文件Key
     */
    String saveSnapshot(Long logId, String beforeData, String afterData);
}
