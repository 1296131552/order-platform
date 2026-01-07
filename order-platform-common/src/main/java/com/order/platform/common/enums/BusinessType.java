package com.order.platform.common.enums;

import lombok.Getter;

/**
 * 业务类型枚举
 *
 * 说明：用于操作日志的多态关联，标识操作关联的业务实体类型
 *
 * @since 1.0.0
 */
@Getter
public enum BusinessType {

    /**
     * 订单
     */
    ORDER("order", "订单"),

    /**
     * 订单行
     */
    ORDER_LINE("order_line", "订单行"),

    /**
     * 发运批次
     */
    SHIPMENT("shipment", "发运批次"),

    /**
     * 快递单
     */
    SHIPMENT_LINE("shipment_line", "快递单"),

    /**
     * 签收明细
     */
    RECEIPT("receipt", "签收明细"),

    /**
     * 客户
     */
    CUSTOMER("customer", "客户"),

    /**
     * 供应商
     */
    SUPPLIER("supplier", "供应商"),

    /**
     * 承运商
     */
    CARRIER("carrier", "承运商"),

    /**
     * 异常记录
     */
    EXCEPTION("exception", "异常记录"),

    /**
     * 附件
     */
    ATTACHMENT("attachment", "附件"),

    /**
     * 用户
     */
    USER("user", "用户"),

    /**
     * 角色
     */
    ROLE("role", "角色"),

    /**
     * 未知类型（默认）
     */
    UNKNOWN("unknown", "未知");

    private final String code;
    private final String desc;

    BusinessType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 代码
     * @return 枚举值，不存在返回 UNKNOWN
     */
    public static BusinessType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN;
        }
        for (BusinessType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
