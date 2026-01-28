package com.company.user.model.dto.base;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BasePermissionDTO {
    public interface Add {}
    public interface Modify {}

    @NotNull(groups = Modify.class, message = "权限ID不能为空")
    private Integer id;

    @NotBlank(groups = {Add.class, Modify.class}, message = "权限名称不能为空")
    private String name;

    private String path;

    private String component;

    private String permissionCode;

    @NotNull(groups = {Add.class, Modify.class}, message = "父节点ID不能为空")
    private Integer parentId;

    private Boolean isValid;
}