package com.company.user.model.dto;

import com.company.user.annotation.ValidPassword;
// import com.company.user.annotation.ValidRoleLimit;
import com.company.user.enums.model.UserSex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddUserDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ValidPassword
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private UserSex sex = UserSex.UNKNOWN;

    @NotEmpty(message = "角色不能为空")
    // @ValidRoleLimit
    private List<Integer> roleIds;
}
