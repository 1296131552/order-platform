package com.company.order.visual.user.dto;

import jakarta.validation.constraints.*; // 参数校验
import lombok.Data;

import java.util.List;
// 更新请求
@Data
public class UserCreateRequest {
    @NotBlank
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    @Pattern(regexp =  "^[a-zA-Z0-9_]+$" , message = "用户名只能包含字母、数字、下划线") // 正则
    String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过20位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private List<Long> roleIds;
    private Long departmentId;
    private String position;
    private String employeeNo;

    @Size(max = 200, message = "备注长度不能超过200位")
    private String remark;
}
