package com.company.order.visual.exception.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常记录实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_exception")
public class ExceptionRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 异常单号（唯一）
     */
    private String exceptionNo;

    /**
     * 业务类型（多态关联：order/order_line/shipment/shipment_line/receipt_detail等）
     */
    private String businessType;

    /**
     * 业务ID（多态关联）
     */
    private Long businessId;

    /**
     * 异常类型
     */
    private String exceptionType;

    /**
     * 异常标题
     */
    private String exceptionTitle;

    /**
     * 异常描述
     */
    private String exceptionDesc;

    /**
     * 处理状态（1-待处理 2-处理中 3-已处理）
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 是否删除（0-否 1-是）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
