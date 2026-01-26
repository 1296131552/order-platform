package com.company.user.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.company.user.enums.model.UserSex;
import com.company.user.model.entity.Role;

@Data
public class UserVO {
    private Integer id;
    private String username;
    private List<Role> roles;
    private String name;
    private String avatarUrl;
    private UserSex sex;
    private String signature;
    private Boolean hasPermission;
    private Boolean isValid;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
