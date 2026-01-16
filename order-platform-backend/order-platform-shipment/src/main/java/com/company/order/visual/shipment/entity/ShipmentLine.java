package com.company.order.visual.shipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.order.visual.common.constant.ShipmentLineStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发运明细实体（快递单）
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_shipment_line")
public class ShipmentLine {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发运批次ID
     */
    private Long shipmentId;

    /**
     * 订单行ID
     */
    private Long orderLineId;

    /**
     * 承运商ID
     */
    private Long carrierId;

    /**
     * 物流单号
     */
    private String trackingNo;

    /**
     * 取货地址
     */
    private String pickupAddress;

    /**
     * 交货地址
     */
    private String deliveryAddress;

    /**
     * 物流状态（使用枚举类型）
     */
    private ShipmentLineStatus status;

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
