package com.order.platform.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.order.platform.common.dto.OperationLogDTO;
import com.order.platform.common.entity.OperationLog;
import com.order.platform.common.mapper.OperationLogMapper;
import com.order.platform.common.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 *
 * 功能说明：
 * - 异步保存操作日志到 MySQL
 * - 详细快照暂时以 JSON 格式存储在 extra_info 字段
 * - 后续可扩展对象存储支持
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 异步保存操作日志
     *
     * 注意：
     * - 使用 @Async 注解实现异步执行
     * - 日志保存失败不影响业务流程
     * - 详细快照暂时存入 extra_info 字段
     */
    @Async("operationLogExecutor")
    @Override
    public void saveAsync(OperationLogDTO logDTO) {
        try {
            // 1. DTO 转实体（使用 Hutool 的 BeanUtil）
            OperationLog entity = BeanUtil.copyProperties(logDTO, OperationLog.class);

            // 2. 设置默认值
            if (entity.getOperationTime() == null) {
                entity.setOperationTime(LocalDateTime.now());
            }
            if (entity.getIsDeleted() == null) {
                entity.setIsDeleted(0);
            }
            // businessId 为空时设置默认值 0（表示无关联业务）
            if (entity.getBusinessId() == null) {
                entity.setBusinessId(0L);
            }

            // 3. 处理数据快照（暂时存入 extra_info）
            if (StrUtil.isNotBlank(logDTO.getBeforeData()) || StrUtil.isNotBlank(logDTO.getAfterData())) {
                String snapshot = buildSnapshotJson(logDTO.getBeforeData(), logDTO.getAfterData());
                entity.setExtraInfo(snapshot);
            }

            // 4. 使用 MyBatis-Plus 的 insert 方法保存
            operationLogMapper.insert(entity);

            log.debug("操作日志保存成功: logId={}, businessType={}, businessId={}",
                    entity.getId(), entity.getBusinessType(), entity.getBusinessId());

        } catch (Exception e) {
            // 日志保存失败不应影响业务流程，仅记录错误日志
            log.error("操作日志保存失败: operationType={}, businessType={}, error={}",
                    logDTO.getOperationType(), logDTO.getBusinessType(), e.getMessage(), e);
        }
    }

    /**
     * 保存数据快照（暂时未实现对象存储）
     *
     * TODO：后续集成对象存储（OSS/MinIO）后实现
     */
    @Override
    public String saveSnapshot(Long logId, String beforeData, String afterData) {
        // 暂时返回空字符串，表示未使用对象存储
        // 后续可集成阿里云 OSS 或 MinIO
        log.warn("对象存储暂未实现，快照数据将存入 extra_info 字段");
        return "";
    }

    /**
     * 构建快照 JSON 字符串
     *
     * @param beforeData 操作前数据
     * @param afterData  操作后数据
     * @return JSON 字符串
     */
    private String buildSnapshotJson(String beforeData, String afterData) {
        StringBuilder json = new StringBuilder("{");
        if (StrUtil.isNotBlank(beforeData)) {
            json.append("\"before\":").append(beforeData);
        }
        if (StrUtil.isNotBlank(afterData)) {
            if (json.length() > 1) {
                json.append(",");
            }
            json.append("\"after\":").append(afterData);
        }
        json.append("}");
        return json.toString();
    }
}
