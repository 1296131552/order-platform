package com.company.user.model.dto;


import com.company.user.enums.model.PermissionFtType;
import com.company.user.model.dto.base.BasePermissionDTO;
import jakarta.validation.constraints.NotNull;

public class PermissionFtDTO extends BasePermissionDTO {
    @NotNull(groups = {Add.class, Modify.class}, message = "权限类型不能为空")
    private PermissionFtType type;

    @NotNull(groups = {Add.class, Modify.class}, message = "是否需要验证不能为空")
    private Boolean needVerify;
}