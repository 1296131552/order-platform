package com.order.platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志DTO
 *
 * 用于接收和传递操作日志数据
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO {

    /**
     * 主键ID（保存后自动填充）
     */
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人用户编号（业务标识，如 USER001）
     */
    private String operatorUserCode;

    /**
     * 操作人工号
     */
    private String operatorEmployeeNo;

    /**
     * 操作人部门ID
     */
    private Long operatorDepartmentId;

    /**
     * 操作人部门名称
     */
    private String operatorDepartmentName;

    /**
     * 操作人职位
     */
    private String operatorPosition;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 业务实体编号
     */
    private String businessNo;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String operationModule;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 操作结果描述
     */
    private String resultDesc;

    /**
     * 操作IP地址
     */
    private String operationIp;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作耗时（毫秒）
     */
    private Integer operationDuration;

    /**
     * 数据快照Key
     */
    private String snapshotKey;

    /**
     * 扩展信息（JSON）
     */
    private String extraInfo;

    /**
     * 操作前数据快照（JSON字符串）
     */
    private String beforeData;

    /**
     * 操作后数据快照（JSON字符串）
     */
    private String afterData;
}
