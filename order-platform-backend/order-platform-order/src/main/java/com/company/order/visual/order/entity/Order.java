package com.company.order.visual.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.order.visual.common.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_order")
public class Order {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号（唯一）
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户名称（冗余）
     */
    private String customerName;

    /**
     * 订单状态（使用枚举类型）
     */
    private OrderStatus status;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 交货日期
     */
    private LocalDateTime deliveryDate;

    /**
     * 备注
     */
    private String remark;

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
