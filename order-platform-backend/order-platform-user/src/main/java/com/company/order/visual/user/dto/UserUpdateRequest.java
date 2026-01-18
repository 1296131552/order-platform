package com.company.order.visual.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class UserUpdateRequest {
    private Long id;

    @Size(max = 20, message = "姓名长度不能超过20位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;
    private Boolean isEnabled;
    private Long departmentId;
    private String position;
    private String employeeNo;

    @Size(max = 200, message = "备注长度不能超过200位")
    private String remark;

    private List<Long> roleIds;    
}
