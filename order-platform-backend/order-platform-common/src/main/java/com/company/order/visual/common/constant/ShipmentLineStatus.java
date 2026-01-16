package com.company.order.visual.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 发运明细状态（快递单状态）枚举
 *
 * <p>状态流转：CREATED → PICKED_UP → IN_TRANSIT → DELIVERED → RECEIVED</p>
 *
 * @author Order Platform Team
 */
public enum ShipmentLineStatus {

    /**
     * 已创建 - 快递单已生成，等待揽收
     */
    CREATED("created", "已创建"),

    /**
     * 已取货 - 快递公司已揽收
     */
    PICKED_UP("picked_up", "已取货"),

    /**
     * 运输中 - 快递正在运输
     */
    IN_TRANSIT("in_transit", "运输中"),

    /**
     * 已送达 - 快递员已将包裹送达指定地点（如放在门口、快递柜）
     * <p>注意：此时收货人尚未签收，是快递公司的"送达"状态</p>
     */
    DELIVERED("delivered", "已送达"),

    /**
     * 已签收 - 收货人已确认签收（最终状态）
     * <p>此状态触发签收流程，记录签收数量和差异</p>
     */
    RECEIVED("received", "已签收"),

    /**
     * 异常 - 快递过程中出现异常（丢件、延误、破损等）
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

    ShipmentLineStatus(String value, String desc) {
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
