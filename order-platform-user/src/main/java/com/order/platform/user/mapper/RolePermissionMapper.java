package com.order.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.order.platform.user.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联 Mapper
 *
 * 功能说明：
 * - 角色权限关联基础CRUD操作（继承BaseMapper）
 * - 查询角色的权限代码列表
 * - 查询拥有指定权限的角色列表
 * - 权限验证相关查询
 *
 * @since 1.0.0
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 查询角色的权限代码列表
     *
     * 查询条件：
     * - 角色ID匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 用户登录时查询权限
     * - 权限验证
     * - 角色权限展示
     *
     * @param roleId 角色ID
     * @return 权限代码列表，如["ORDER:VIEW", "ORDER:CREATE", "SHIPMENT:VIEW"]
     */
    @Select("SELECT permission_code FROM t_role_permission " +
            "WHERE role_id = #{roleId} " +
            "AND is_deleted = 0")
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询拥有指定权限的角色列表
     *
     * 查询条件：
     * - 权限代码匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 权限反向查询
     * - 角色权限分析
     * - 权限分配情况统计
     *
     * @param permissionCode 权限代码（如"ORDER:VIEW"）
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM t_role_permission " +
            "WHERE permission_code = #{permissionCode} " +
            "AND is_deleted = 0")
    List<RolePermission> selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询角色的所有权限关联记录
     *
     * 查询条件：
     * - 角色ID匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 角色权限详情展示
     * - 角色权限管理
     *
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM t_role_permission " +
            "WHERE role_id = #{roleId} " +
            "AND is_deleted = 0 " +
            "ORDER BY id ASC")
    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查角色是否拥有指定权限
     *
     * 查询条件：
     * - 角色ID匹配
     * - 权限代码匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 权限快速验证
     * - 权限检查
     *
     * @param roleId 角色ID
     * @param permissionCode 权限代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(*) FROM t_role_permission " +
            "WHERE role_id = #{roleId} " +
            "AND permission_code = #{permissionCode} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    int countByRoleIdAndPermissionCode(@Param("roleId") Long roleId,
                                       @Param("permissionCode") String permissionCode);

    /**
     * 统计角色的权限数量
     *
     * 统计条件：
     * - 角色ID匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 角色权限统计
     * - 角色权限分析
     *
     * @param roleId 角色ID
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM t_role_permission " +
            "WHERE role_id = #{roleId} " +
            "AND is_deleted = 0")
    int countByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表批量查询权限代码
     *
     * 查询条件：
     * - 角色ID在列表中
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 用户权限查询（多角色）
     * - 批量权限验证
     * - 避免N+1查询问题
     *
     * @param roleIds 角色ID列表
     * @return 权限代码列表（可能有重复，需要去重）
     */
    @Select("<script>" +
            "SELECT permission_code FROM t_role_permission " +
            "WHERE is_deleted = 0 " +
            "AND role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "</script>")
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 查询所有权限代码（去重）
     *
     * 查询条件：
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 权限列表展示
     * - 权限统计分析
     * - 权限枚举生成
     *
     * @return 所有权限代码列表（去重）
     */
    @Select("SELECT DISTINCT permission_code FROM t_role_permission " +
            "WHERE is_deleted = 0 " +
            "ORDER BY permission_code ASC")
    List<String> selectAllDistinctPermissionCodes();

    /**
     * 查询指定模块的所有权限代码
     *
     * 查询条件：
     * - 权限代码以指定模块开头（如"ORDER:"）
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 模块权限查询
     * - 模块权限统计
     *
     * @param module 权限模块（如"ORDER"）
     * @return 该模块的权限代码列表
     */
    @Select("SELECT DISTINCT permission_code FROM t_role_permission " +
            "WHERE permission_code LIKE CONCAT(#{module}, ':%') " +
            "AND is_deleted = 0 " +
            "ORDER BY permission_code ASC")
    List<String> selectPermissionCodesByModule(@Param("module") String module);

    /**
     * 统计指定权限被分配给多少个角色
     *
     * 统计条件：
     * - 权限代码匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 权限使用情况分析
     * - 权限分配统计
     *
     * @param permissionCode 权限代码
     * @return 拥有该权限的角色数量
     */
    @Select("SELECT COUNT(DISTINCT role_id) FROM t_role_permission " +
            "WHERE permission_code = #{permissionCode} " +
            "AND is_deleted = 0")
    int countRolesByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 删除角色的所有权限（软删除）
     *
     * 注意：这是逻辑删除，不是物理删除
     * 实际实现需要在Service层调用update方法
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    default int deleteByRoleId(Long roleId) {
        // 使用MyBatis-Plus的UpdateWrapper实现逻辑删除
        // 或者使用XML映射文件
        return 0; // 占位，实际在Service实现
    }

    /**
     * 删除角色的指定权限（软删除）
     *
     * 注意：这是逻辑删除，不是物理删除
     *
     * @param roleId 角色ID
     * @param permissionCode 权限代码
     * @return 删除的记录数
     */
    default int deleteByRoleIdAndPermissionCode(Long roleId, String permissionCode) {
        // 使用MyBatis-Plus的UpdateWrapper实现逻辑删除
        // 或者使用XML映射文件
        return 0; // 占位，实际在Service实现
    }
}
