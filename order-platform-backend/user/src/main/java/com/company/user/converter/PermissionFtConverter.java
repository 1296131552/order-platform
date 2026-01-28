package com.company.user.converter;

import com.company.user.model.dto.PermissionFtDTO;
import com.company.user.model.entity.PermissionFt;
import com.company.user.model.vo.PermissionFtVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * PermissionFt转换接口
 */
@Mapper(componentModel = "spring")
public interface PermissionFtConverter {

    /**
     * 将PermissionFt对象转换为PermissionVO对象
     * @param permissionFt PermissionFt对象
     * @return PermissionVO对象
     */
    @Mapping(source = ".", target = "node")
    PermissionFtVO toPermissionVO(PermissionFt permissionFt);

    /**
     * 将PermissionFt列表转换为PermissionVO列表
     * @param permissionFtList PermissionFt列表
     * @return PermissionVO列表
     */
    List<PermissionFtVO> toPermissionVOS(List<PermissionFt> permissionFtList);

     /**
      * 将PermissionFtDTO对象转换为PermissionFt对象
      * @param permissionFtDTO PermissionFtDTO对象
      * @return PermissionFt对象
      */
     PermissionFt toPermission(PermissionFtDTO permissionFtDTO);
}

