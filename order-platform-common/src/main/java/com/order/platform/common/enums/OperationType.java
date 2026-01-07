package com.order.platform.common.enums;

import lombok.Getter;

/**
 * 操作类型枚举
 *
 * 说明：标识操作的类型，如创建、更新、删除等
 *
 * @since 1.0.0
 */
@Getter
public enum OperationType {

    /**
     * 创建
     */
    CREATE("CREATE", "创建"),

    /**
     * 更新
     */
    UPDATE("UPDATE", "更新"),

    /**
     * 删除
     */
    DELETE("DELETE", "删除"),

    /**
     * 查看
     */
    VIEW("VIEW", "查看"),

    /**
     * 导出
     */
    EXPORT("EXPORT", "导出"),

    /**
     * 导入
     */
    IMPORT("IMPORT", "导入"),

    /**
     * 审核
     */
    AUDIT("AUDIT", "审核"),

    /**
     * 确认
     */
    CONFIRM("CONFIRM", "确认"),

    /**
     * 审批
     */
    APPROVE("APPROVE", "审批"),

    /**
     * 取消
     */
    CANCEL("CANCEL", "取消"),

    /**
     * 登录
     */
    LOGIN("LOGIN", "登录"),

    /**
     * 登出
     */
    LOGOUT("LOGOUT", "登出"),

    /**
     * 其他（默认）
     */
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;

    OperationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 代码
     * @return 枚举值，不存在返回 OTHER
     */
    public static OperationType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return OTHER;
        }
        for (OperationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
