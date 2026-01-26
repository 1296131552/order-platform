package com.company.user.model.dto;

import com.company.user.enums.model.UserSex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @NotNull(message = "ID不能为空")
    private Integer id;
    @Size(min = 2, max = 20, message = "名称长度必须大于等于2且小于等于20")
    private String name;
    private String avatarUrl;
    private UserSex sex;
    @Size(max = 200, message = "签名长度必须小于等于200")
    private String signature;
}
