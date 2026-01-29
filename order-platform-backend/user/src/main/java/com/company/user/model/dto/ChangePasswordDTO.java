package com.company.user.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import com.company.user.annotation.ValidPassword;

@Data
public class ChangePasswordDTO {
    /**
     * 当前密码
     */
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;
    
    /**
     * 新密码
     * 1. 必须包含数字和小写字母
     * 2. 必须包含大写字母或特殊字符(@或_)其中一种
     * 3. 仅能包含数字、大小写字母、@、_
     */
    @ValidPassword
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}