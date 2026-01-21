package com.company.order.visual.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * <p>
 * 职责：
 * - insert 时自动填充：createdAt, createdBy, updatedAt, updatedBy, isDeleted
 * - update 时自动填充：updatedAt, updatedBy
 * <p>
 * 使用方式：
 * - 在实体字段上添加 @TableField(fill = FieldFill.INSERT) 或 @TableField(fill = FieldFill.INSERT_UPDATE)
 * - 无需手动设置审计字段
 * <p>
 * 注意：操作人 ID 需要通过 ThreadLocal 传递，此处暂设为 -1 表示系统操作
 *
 * @author Order Platform Team
 */
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    /**
     * 操作人 ID 存储键（ThreadLocal）
     */
    private static final ThreadLocal<Long> OPERATOR_ID = new ThreadLocal<>();

    /**
     * 设置当前操作人 ID
     */
    public static void setOperatorId(Long operatorId) {
        OPERATOR_ID.set(operatorId);
    }

    /**
     * 获取当前操作人 ID
     */
    public static Long getOperatorId() {
        Long id = OPERATOR_ID.get();
        return id != null ? id : -1L;  // -1 表示系统操作
    }

    /**
     * 清除当前操作人 ID
     */
    public static void clearOperatorId() {
        OPERATOR_ID.remove();
    }

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = getOperatorId();

        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createdBy", Long.class, operatorId);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, operatorId);
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, false);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, getOperatorId());
    }
}
