package com.order.platform.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * 表说明：记录用户对业务实体的操作，用于审计追溯
 * 存储策略：混合存储（核心信息存 MySQL，详细快照存对象存储）
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_operation_log")
public class OperationLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID（-1表示系统操作）
     */
    private Long operatorId;

    /**
     * 操作人姓名（冗余字段，便于展示）
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
     * 操作人部门ID（-1表示未分配部门）
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
     * 业务类型（多态关联）：order/order_line/shipment/shipment_line等
     */
    private String businessType;

    /**
     * 业务ID（多态关联）
     */
    private Long businessId;

    /**
     * 业务实体编号（冗余字段，便于展示）
     */
    private String businessNo;

    /**
     * 操作类型：CREATE/UPDATE/DELETE/VIEW等
     */
    private String operationType;

    /**
     * 操作模块：ORDER/PARTNER/SHIPMENT等
     */
    private String operationModule;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作结果：SUCCESS/FAILED/PARTIAL
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
     * 请求方法：GET/POST/PUT/DELETE
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
     * 数据快照文件Key（对象存储）
     */
    private String snapshotKey;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;

    /**
     * 是否删除：0-未删除,1-已删除
     */
    private Integer isDeleted;
}
