package com.company.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleDTO {
    
    // 验证组接口
    public interface Add {}
    public interface Modify {}
    public interface GlobalAdd {}
    public interface GlobalModify {}
    
    @NotNull(groups = {Modify.class, GlobalModify.class}, message = "角色ID不能为空")
    private Integer id;
    
    @NotBlank(groups = {Add.class, Modify.class, GlobalAdd.class, GlobalModify.class}, message = "角色名不能为空")
    private String name;
    
    @NotBlank(groups = {Add.class, Modify.class, GlobalAdd.class, GlobalModify.class}, message = "角色描述不能为空")
    private String description;
    
    @NotNull(groups = {Add.class, Modify.class}, message = "父ID不能为空")
    private Integer parentNodeId;
}