package com.order.platform.common.annotation;

import com.order.platform.common.enums.BusinessType;
import com.order.platform.common.enums.OperationModule;
import com.order.platform.common.enums.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * 使用说明：
 * - 标记在 Controller 或 Service 方法上，自动记录操作日志
 * - 支持多态关联（businessType + businessId）
 * - 支持数据快照（before/after）存储到对象存储
 * - 异步保存，不影响业务性能
 *
 * 示例：
 * <pre>
 * {@code
 * @PostMapping("/order/create")
 * @OperationLog(
 *     business = BusinessType.ORDER,
 *     type = OperationType.CREATE,
 *     module = OperationModule.ORDER,
 *     description = "创建订单",
 *     businessId = "#result.id",
 *     businessNo = "#result.orderNo",
 *     saveSnapshot = true
 * )
 * public Result<Order> createOrder(@RequestBody OrderDTO dto) {
 *     // 业务逻辑
 * }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 业务类型（多态关联）
     */
    BusinessType business() default BusinessType.UNKNOWN;

    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;

    /**
     * 操作模块
     */
    OperationModule module() default OperationModule.SYSTEM;

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否保存数据快照（默认false）
     * 注意：快照数据会存储到对象存储（OSS/MinIO）
     */
    boolean saveSnapshot() default false;

    /**
     * 业务ID表达式（SpEL）
     * 示例：#dto.id、#result.id、#orderId
     */
    String businessId() default "";

    /**
     * 业务编号表达式（SpEL）
     * 示例：#dto.orderNo、#result.orderNo
     */
    String businessNo() default "";

    /**
     * 业务名称表达式（SpEL）
     * 示例：#dto.customerName、#result.customerName
     */
    String businessName() default "";

    /**
     * 操作人ID表达式（SpEL）
     * 用于登录/注册等无法从 CurrentUserHolder 获取用户的场景
     * 示例：#result.data.userInfo.id
     */
    String operatorId() default "";

    /**
     * 操作人姓名表达式（SpEL）
     * 示例：#result.data.userInfo.realName
     */
    String operatorName() default "";

    /**
     * 操作人用户编号表达式（SpEL）
     * 示例：#result.data.userInfo.userCode
     */
    String operatorUserCode() default "";

    /**
     * 操作人工号表达式（SpEL）
     * 示例：#result.data.userInfo.employeeNo
     */
    String operatorEmployeeNo() default "";

    /**
     * 操作人职位表达式（SpEL）
     * 示例：#result.data.userInfo.position
     */
    String operatorPosition() default "";
}
