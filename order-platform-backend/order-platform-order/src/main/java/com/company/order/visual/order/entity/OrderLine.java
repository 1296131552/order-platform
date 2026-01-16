package com.company.order.visual.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.order.visual.common.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单行实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_order_line")
public class OrderLine {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 行号
     */
    private Integer lineNo;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 行金额
     */
    private BigDecimal lineAmount;

    /**
     * 订单行状态（使用枚举类型）
     */
    private OrderStatus status;

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
