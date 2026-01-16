package com.company.order.visual.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 合作方类型枚举
 *
 * @author Order Platform Team
 */
public enum PartnerType {

    /**
     * 供应商 - 提供货物的合作方
     */
    SUPPLIER("supplier", "供应商"),

    /**
     * 承运商 - 提供物流服务的合作方
     */
    CARRIER("carrier", "承运商"),

    /**
     * 客户 - 下单的购买方
     */
    CUSTOMER("customer", "客户");

    /**
     * 存储到数据库的值（MyBatis-Plus 使用）
     */
    @EnumValue
    private final String value;

    /**
     * 描述
     */
    private final String desc;

    PartnerType(String value, String desc) {
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
