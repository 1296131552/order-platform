package com.company.user.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

/**
 * 密码验证注解
 * 密码规则：
 * <ol>
 *     <li>必须至少8位且包含数字和小写字母</li>
 *     <li>必须包含大写字母或特殊字符(@或_)其中一种</li>
 *     <li>只能包含数字、大小写字母、@、_</li>
 * </ol>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
@Pattern.List({
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z]).{8,}$",
        message = "密码必须至少8位且包含数字和小写字母"
    ),
    @Pattern(
        regexp = "^(?=.*[A-Z@_]).+$",
        message = "密码必须包含大写字母或特殊字符(@或_)其中一种"
    ),
    @Pattern(
        regexp = "^[a-zA-Z0-9@_]+$",
        message = "密码只能包含数字、大小写字母、@、_"
    )
})
public @interface ValidPassword {
    String message() default "密码格式不正确";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
