package com.company.order.visual.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 订单状态枚举
 *
 * @author Order Platform Team
 */
public enum OrderStatus {

    /**
     * 草稿 - 订单创建后未提交执行
     */
    DRAFT("draft", "草稿"),

    /**
     * 执行中 - 订单已确认，正在执行发运
     */
    EXECUTING("executing", "执行中"),

    /**
     * 部分到货 - 订单部分产品已签收
     */
    PARTIALLY_RECEIVED("partially_received", "部分到货"),

    /**
     * 完成 - 订单全部产品已签收
     */
    COMPLETED("completed", "完成"),

    /**
     * 已归档 - 订单完成并存档
     */
    ARCHIVED("archived", "已归档");

    /**
     * 存储到数据库的值（MyBatis-Plus 使用）
     */
    @EnumValue
    private final String value;

    /**
     * 描述
     */
    private final String desc;

    OrderStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 返回存储值（用于 API 响应和数据库存储）
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
