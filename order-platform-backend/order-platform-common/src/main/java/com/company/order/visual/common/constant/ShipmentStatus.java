package com.company.order.visual.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 发运批次状态枚举
 *
 * @author Order Platform Team
 */
public enum ShipmentStatus {

    /**
     * 待提货 - 发运计划已创建，等待提货
     */
    PENDING("pending", "待提货"),

    /**
     * 在途 - 已提货，运输中
     */
    IN_TRANSIT("in_transit", "在途"),

    /**
     * 已到货 - 快递已送达目的地
     */
    DELIVERED("delivered", "已到货"),

    /**
     * 异常 - 发运过程中出现异常
     */
    EXCEPTION("exception", "异常");

    /**
     * 存储到数据库的值（MyBatis-Plus 使用）
     */
    @EnumValue
    private final String value;

    /**
     * 描述
     */
    private final String desc;

    ShipmentStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
