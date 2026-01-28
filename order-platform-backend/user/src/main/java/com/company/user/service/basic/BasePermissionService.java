package com.company.user.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.enums.model.PermissionType;
import com.company.user.model.entity.base.BasePermission;

import java.util.List;

/**
 * 基础权限服务接口
 * @param <T> 基础权限实体类型
 */
public interface BasePermissionService<T extends BasePermission>
        extends IService<T> {
    /**
     * 根据权限类型和关键词获取权限列表
     * @param type 权限类型
     * @param keyword 关键词
     * @return 权限列表
     */
    <E extends PermissionType> List<T> getPermissionByTypeAndKeyword(E type, String keyword);

    /**
     * 根据权限类型列表和关键词查询权限列表
     * @param types 权限类型列表
     * @param keyword 关键词
     * @return 权限列表
     */
    <E extends PermissionType> List<T> getPermissionByTypesAndKeyword(List<E> types, String keyword);

    /**
     * 根据权限类型查询权限列表
     * @param type 权限类型
     * @return 权限列表
     */
    <E extends PermissionType> List<T> getPermissionByTypes(E type);

    /**
     * 根据权限类型列表查询权限列表
     * @param types 权限类型列表
     * @return 权限列表
     */
    <E extends PermissionType> List<T> getPermissionByTypes(List<E> types);

    /**
     * 获取指定权限ID的所有祖先权限ID（不包含当前权限）
     * @param permissionId 权限ID
     * @return 所有祖先权限ID列表
     */
    List<Integer> getAncestorIds(Integer permissionId);

    /**
     * 获取指定权限ID列表的所有祖先权限ID（不包含当前权限）
     * @param permissionIds 权限ID列表
     * @return 所有祖先权限ID列表
     */
    List<Integer> getAncestorIds(List<Integer> permissionIds);

    /**
     * 获取指定权限ID的所有后代权限ID
     * @param permissionId 权限ID
     * @return 所有后代权限ID列表
     */
    List<Integer> getDescendantIds(Integer permissionId);

    /**
     * 获取指定权限ID列表的所有后代权限ID
     * @param permissionIds 权限ID列表
     * @return 所有后代权限ID列表
     */
    List<Integer> getDescendantIds(List<Integer> permissionIds);

    /**
     * 获取指定权限ID列表的所有权限码
     * @param permissionIds 权限ID列表
     * @return 所有权限码列表
     */
    List<String> getPermissionCodes(List<Integer> permissionIds);

    /**
     * 在指定权限之后插入权限
     * @param permission 权限实体
     * @param frontPermission 前一个权限实体
     */
    void insert(T permission, T frontPermission);

    /**
     * 在指定父节点的前头插入权限
     * @param permission 权限实体
     */
    void headInsert(T permission);

    /**
     * 尾插法插入权限
     * @param permission 权限实体
     */
    void tailInsert(T permission);

    /**
     * 连接权限的邻居节点
     * @param permission 权限实体
     */
    void connectNeighborPermissions(T permission);

    /**
     * 修改权限
     * @param permission 权限实体
     */
    void modifyPermission(T permission);

    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    void deletePermission(Integer permissionId);

    /**
     * 权限上移
     * @param permissionId 权限ID
     */
    void moveUp(Integer permissionId);

    /**
     * 权限下移
     * @param permissionId 权限ID
     */
    void moveDown(Integer permissionId);

    /**
     * 校验权限是否为尾权限
     * @param permission 权限实体
     * @return 是否为尾权限
     */
    Boolean isTailPermission(T permission);

    /**
     * 保存或更新权限
     * @param permission 权限实体
     * @return 权限ID
     */
    Integer saveOrUpdatePermission(T permission);

     /**
     * 权限前移
     * @param permission 权限实体
     * @param length 前移长度，默认1
     */
    void moveForward(T permission, Integer... length);

     /**
     * 权限后移
     * @param permission 权限实体
     * @param length 后移长度，默认1
     */
    void moveBackward(T permission, Integer... length);
}
