package com.company.order.visual.shipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 签收明细实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_receipt_detail")
public class ReceiptDetail {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发运明细ID（快递单ID）
     */
    private Long shipmentLineId;

    /**
     * 签收数量
     */
    private BigDecimal receivedQuantity;

    /**
     * 期望数量
     */
    private BigDecimal expectedQuantity;

    /**
     * 差异数量
     */
    private BigDecimal differenceQuantity;

    /**
     * 差异原因
     */
    private String differenceReason;

    /**
     * 签收人
     */
    private String receiptPerson;

    /**
     * 签收时间
     */
    private LocalDateTime receiptTime;

    /**
     * 状态（1-正常 2-有差异）
     */
    private Integer status;

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
