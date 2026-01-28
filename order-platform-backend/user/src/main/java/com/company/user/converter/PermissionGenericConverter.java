package com.company.user.converter;

import com.company.user.model.dto.PermissionBkDTO;
import com.company.user.model.dto.PermissionFtDTO;
import com.company.user.model.dto.base.BasePermissionDTO;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.entity.PermissionFt;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.model.vo.base.BasePermissionVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用权限转换类
 */
@Component
public class PermissionGenericConverter {
    // 原有的转换器实例
    @Resource
    PermissionBkConverter bkConverter;
    @Resource
    PermissionFtConverter ftConverter;

    /**
     * 将权限entity对象转换为权限VO对象
     * permissionBk 转换为 PermissionBkVO
     * permissionFt 转换为 PermissionFtVO
     * @param permission 权限entity对象
     * @return 权限VO对象
     */
    @SuppressWarnings("unchecked")
    public <T extends BasePermission, VO extends BasePermissionVO<T, VO>> VO toPermissionVO(T permission) {
        if (permission instanceof PermissionBk) {
            return (VO) bkConverter.toPermissionVO((PermissionBk) permission);
        } else if (permission instanceof PermissionFt) {
            return (VO) ftConverter.toPermissionVO((PermissionFt) permission);
        } else {
            throw new IllegalArgumentException("不支持的权限entity类型：" + permission.getClass().getName());
        }
    }

    /**
     * 将权限entity列表转换为权限VO列表
     * List<PermissionBk> 转换为 List<PermissionBkVO>
     * List<PermissionFt> 转换为 List<PermissionFtVO>
     * @param permissions 权限entity列表
     * @return 权限VO列表
     */
    public <T extends BasePermission, VO extends BasePermissionVO<T, VO>> List<VO> toPermissionVOS(List<T> permissions) {
        return permissions.stream()
                .map(this::<T, VO>toPermissionVO)
                .collect(Collectors.toList());
    }

    /**
     * 将权限DTO对象转换为权限entity对象
     * permissionBkDTO 转换为 PermissionBk
     * permissionFtDTO 转换为 PermissionFt
     * @param permissionDTO 权限DTO对象
     * @return 权限entity对象
     */
    @SuppressWarnings("unchecked")
    public <T extends BasePermission, DTO extends BasePermissionDTO> T toPermission(DTO permissionDTO) {
        if (permissionDTO instanceof PermissionBkDTO) {
            return (T) bkConverter.toPermission((PermissionBkDTO) permissionDTO);
        } else if (permissionDTO instanceof PermissionFtDTO) {
            return (T) ftConverter.toPermission((PermissionFtDTO) permissionDTO);
        } else {
            throw new IllegalArgumentException("不支持的权限DTO类型：" + permissionDTO.getClass().getName());
        }
    }
}
