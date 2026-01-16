package com.company.order.visual.shipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.order.visual.common.constant.ShipmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发运批次实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_shipment")
public class Shipment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次号（唯一）
     */
    private String shipmentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 承运商ID
     */
    private Long carrierId;

    /**
     * 提货地址
     */
    private String pickupAddress;

    /**
     * 计划提货时间
     */
    private LocalDateTime pickupTime;

    /**
     * 交货地址
     */
    private String deliveryAddress;

    /**
     * 计划交货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 实际提货时间
     */
    private LocalDateTime actualPickupTime;

    /**
     * 实际交货时间
     */
    private LocalDateTime actualDeliveryTime;

    /**
     * 发运状态（使用枚举类型）
     */
    private ShipmentStatus status;

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
