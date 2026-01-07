package com.order.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.order.platform.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper
 *
 * 功能说明：
 * - 角色基础CRUD操作（继承BaseMapper）
 * - 角色代码查询（唯一性验证）
 * - 角色列表查询（启用、系统角色、角色类型）
 * - 角色权限验证相关查询
 *
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色代码查询角色
     *
     * 查询条件：
     * - 角色代码匹配
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 角色代码唯一性验证
     * - 角色信息查询
     *
     * @param roleCode 角色代码
     * @return 角色实体，不存在返回null
     */
    @Select("SELECT * FROM t_role " +
            "WHERE role_code = #{roleCode} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有启用的角色
     *
     * 查询条件：
     * - 启用（is_enabled = 1）
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 角色下拉选择列表
     * - 角色分配功能
     *
     * @return 启用的角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE is_enabled = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectEnabledRoles();

    /**
     * 根据角色类型查询角色
     *
     * 查询条件：
     * - 角色类型匹配
     * - 启用（is_enabled = 1）
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 区分业务角色和系统角色
     * - 角色分类展示
     *
     * @param roleType 角色类型（BUSINESS/SYSTEM）
     * @return 该类型的角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE role_type = #{roleType} " +
            "AND is_enabled = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectByRoleType(@Param("roleType") String roleType);

    /**
     * 查询所有系统角色
     *
     * 查询条件：
     * - 系统角色（is_system = 1）
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 判断角色是否可删除
     * - 系统角色管理
     *
     * @return 系统角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE is_system = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectSystemRoles();

    /**
     * 查询所有业务角色
     *
     * 查询条件：
     * - 业务角色（role_type = 'BUSINESS'）
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 业务角色分配
     * - 业务角色管理
     *
     * @return 业务角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE role_type = 'BUSINESS' " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectBusinessRoles();

    /**
     * 查询可删除的角色
     *
     * 查询条件：
     * - 用户自定义角色（is_system = 0）
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 判断角色是否可删除
     * - 角色删除功能
     *
     * @return 可删除的角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE is_system = 0 " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectDeletableRoles();

    /**
     * 根据数据权限类型查询角色
     *
     * 查询条件：
     * - 数据权限类型匹配
     * - 启用（is_enabled = 1）
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 数据权限统计
     * - 角色筛选
     *
     * @param dataScopeType 数据权限类型（1-全部、2-部门、3-本人、4-自定义）
     * @return 该数据权限类型的角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE data_scope_type = #{dataScopeType} " +
            "AND is_enabled = 1 " +
            "AND is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectByDataScopeType(@Param("dataScopeType") Integer dataScopeType);

    /**
     * 查询所有角色（包括禁用的角色）
     *
     * 查询条件：
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 角色管理列表
     * - 角色统计分析
     *
     * @return 所有角色列表
     */
    @Select("SELECT * FROM t_role " +
            "WHERE is_deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC")
    List<Role> selectAllRoles();

    /**
     * 根据角色ID列表批量查询角色
     *
     * 查询条件：
     * - 角色ID在列表中
     * - 未删除（is_deleted = 0）
     * - 按sort_order升序排序
     *
     * 用于：
     * - 用户角色信息展示
     * - 批量角色查询（避免N+1问题）
     *
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    List<Role> selectBatchIds(List<Long> roleIds);

    /**
     * 统计角色数量
     *
     * 统计条件：
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 角色管理统计
     * - 数据分析
     *
     * @return 角色总数
     */
    @Select("SELECT COUNT(*) FROM t_role " +
            "WHERE is_deleted = 0")
    int countRoles();

    /**
     * 统计启用的角色数量
     *
     * 统计条件：
     * - 启用（is_enabled = 1）
     * - 未删除（is_deleted = 0）
     *
     * 用于：
     * - 角色管理统计
     * - 数据分析
     *
     * @return 启用的角色总数
     */
    @Select("SELECT COUNT(*) FROM t_role " +
            "WHERE is_enabled = 1 " +
            "AND is_deleted = 0")
    int countEnabledRoles();
}
