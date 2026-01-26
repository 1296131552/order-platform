package com.company.user.model.dto;

import com.company.user.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @ValidPassword
    private String password;
}