package com.company.order.visual.partner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合作方实体（统一表：供应商/承运商/客户）
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_partner")
public class Partner {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合作方编码（唯一）
     */
    private String partnerNo;

    /**
     * 合作方名称
     */
    private String partnerName;

    /**
     * 合作方类型（supplier-供应商 / carrier-承运商 / customer-客户）
     */
    private String partnerType;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 省份
     */
    private String addressProvince;

    /**
     * 城市
     */
    private String addressCity;

    /**
     * 区县
     */
    private String addressDistrict;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 准时率（百分比）
     */
    private BigDecimal onTimeRate;

    /**
     * 异常率（百分比）
     */
    private BigDecimal exceptionRate;

    /**
     * 状态（1-正常 0-停用）
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
