package com.order.platform.common.enums;

import lombok.Getter;

/**
 * 操作模块枚举
 *
 * 说明：标识操作所属的功能模块
 *
 * @since 1.0.0
 */
@Getter
public enum OperationModule {

    /**
     * 订单管理
     */
    ORDER("ORDER", "订单管理"),

    /**
     * 合作方管理
     */
    PARTNER("PARTNER", "合作方管理"),

    /**
     * 发运管理
     */
    SHIPMENT("SHIPMENT", "发运管理"),

    /**
     * 签收管理
     */
    RECEIPT("RECEIPT", "签收管理"),

    /**
     * 附件管理
     */
    ATTACHMENT("ATTACHMENT", "附件管理"),

    /**
     * 异常管理
     */
    EXCEPTION("EXCEPTION", "异常管理"),

    /**
     * 可视化
     */
    VISUALIZATION("VISUALIZATION", "可视化"),

    /**
     * 看板
     */
    DASHBOARD("DASHBOARD", "看板"),

    /**
     * 系统管理
     */
    SYSTEM("SYSTEM", "系统管理"),

    /**
     * 用户管理
     */
    USER("USER", "用户管理"),

    /**
     * 未知模块（默认）
     */
    UNKNOWN("UNKNOWN", "未知");

    private final String code;
    private final String desc;

    OperationModule(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 代码
     * @return 枚举值，不存在返回 UNKNOWN
     */
    public static OperationModule fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return UNKNOWN;
        }
        for (OperationModule module : values()) {
            if (module.code.equals(code)) {
                return module;
            }
        }
        return UNKNOWN;
    }
}
