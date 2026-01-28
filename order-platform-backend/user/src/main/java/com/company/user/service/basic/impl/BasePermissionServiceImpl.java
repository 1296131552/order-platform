package com.company.user.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.user.constant.PermissionConstant;
import com.company.user.enums.model.PermissionType;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.service.basic.BasePermissionService;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基础权限服务实现类
 * @param <M> 基础权限映射器类型
 * @param <T> 基础权限实体类型
 */
public class BasePermissionServiceImpl<
        M extends BaseMapper<T>,
        T extends BasePermission
        >
        extends ServiceImpl<M, T>
        implements BasePermissionService<T> {

    /**
     * 根据权限类型和关键字查询权限列表
     * @param type 权限类型
     * @param keyword 权限名称关键字
     * @return 权限列表
     */
    @Override
    public <E extends PermissionType> List<T> getPermissionByTypeAndKeyword(E type, String keyword) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (keyword != null) {
            wrapper.like("name", keyword);
        }
        return list(wrapper);
    }

    /**
     * 根据权限类型列表和关键字查询权限列表
     * @param types 权限类型列表
     * @param keyword 权限名称关键字
     * @return 权限列表
     */
    @Override
    public <E extends PermissionType> List<T> getPermissionByTypesAndKeyword(List<E> types, String keyword) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (CollectionUtils.isEmpty(types)) {
            return List.of();
        }
        wrapper.in("type", types);
        wrapper.orderByAsc("parent_id")
                .orderByAsc("`order`");
        if (keyword != null) {
            wrapper.like("name", keyword);
        }
        return list(wrapper);
    }

    /**
     * 根据权限类型查询权限列表
     * @param type 权限类型
     * @return 权限列表
     */
    @Override
    public <E extends PermissionType> List<T> getPermissionByTypes(E type) {
        return getPermissionByTypeAndKeyword(type, null);
    }

    /**
     * 根据权限类型列表查询权限列表
     * @param types 权限类型列表
     * @return 权限列表
     */
    @Override
    public <E extends PermissionType> List<T> getPermissionByTypes(List<E> types) {
        return getPermissionByTypesAndKeyword(types, null);
    }

    /**
     * 获取指定权限ID的所有祖先权限ID（不包含当前权限）
     * @param permissionId 权限ID
     * @return 所有祖先权限ID列表
     */
    @Override
    public List<Integer> getAncestorIds(Integer permissionId) {
        List<Integer> ancestorIds = new ArrayList<>();
        T permission = getById(permissionId);
        while (Objects.nonNull(permission) && !Objects.equals(permission.getParentId(), PermissionConstant.ROOT_PERMISSION_PARENT_ID)) {
            ancestorIds.add(permission.getParentId());
            permission = getById(permission.getParentId());
        }
        return ancestorIds;
    }

    /**
     * 获取指定权限ID列表的所有祖先权限ID（不包含当前权限）
     * @param permissionIds 权限ID列表
     * @return 所有祖先权限ID列表
     */
    @Override
    public List<Integer> getAncestorIds(List<Integer> permissionIds) {
        return permissionIds.stream()
                .map(this::getAncestorIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定权限ID的所有后代权限ID
     * @param permissionId 权限ID
     * @return 所有后代权限ID列表
     */
    @Override
    public List<Integer> getDescendantIds(Integer permissionId) {
        List<Integer> descendantIds = new ArrayList<>();
        List<T> permissions = list();

        Map<Integer, List<T>> parentChildMap = T.buildParentChildMap(permissions);

        List<T> currentLevel = parentChildMap.get(permissionId);
        while (!CollectionUtils.isEmpty(currentLevel)) {
            List<T> nextLevel = new ArrayList<>();
            for (T permission : currentLevel) {
                descendantIds.add(permission.getId());
                nextLevel.addAll(parentChildMap.getOrDefault(permission.getId(), new ArrayList<>()));
            }
            currentLevel = nextLevel;
        }
        return descendantIds;
    }

    /**
     * 获取指定权限ID列表的所有后代权限ID
     * @param permissionIds 权限ID列表
     * @return 所有后代权限ID列表
     */
    @Override
    public List<Integer> getDescendantIds(List<Integer> permissionIds) {
        return permissionIds.stream()
                .map(this::getDescendantIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定权限ID列表的所有权限码
     * @param permissionIds 权限ID列表
     * @return 所有权限码列表
     */
    @Override
    public List<String> getPermissionCodes(List<Integer> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return List.of();
        }
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.in("id", permissionIds)
                .eq("is_valid", true);
        List<T> permissions = list(wrapper);
        return T.extractPermissionCodes(permissions);
    }

    /**
     * 在指定权限之后插入权限
     * @param permission 权限实体
     * @param frontPermission 前一个权限实体
     */
    @Override
    public void insert(T permission, T frontPermission) {
        T behindPermission = getBehindNode(frontPermission);    // 获取frontPermission的behindPermission
        if (Objects.nonNull(behindPermission)) { moveBackward(behindPermission); }

        permission.setOrder(frontPermission.getOrder() + 1);
        saveOrUpdatePermission(permission);  // 保存或更新权限，获取权限ID
    }

    /**
     * 在指定父节点的前头插入权限
     * @param permission 权限实体
     */
    @Override
    public void headInsert(T permission) {
        T headPermission = getHeadNode(permission.getParentId());   // 获取指定父节点的头子节点

        // 如果父节点下有子节点，将headPermission及其后的权限后移一位
        if(Objects.nonNull(headPermission)) { moveBackward(headPermission); }

        saveOrUpdatePermission(permission);
    }

    /**
     * 尾插法插入权限
     * @param permission 权限实体
     */
    @Override
    public void tailInsert(T permission) {
        T tailPermission = getTailNode(permission.getParentId());   // 获取指定父节点的尾子节点

        if (Objects.isNull(tailPermission)) {   // 如果父节点下没有子节点，直接头插法添加权限
            headInsert(permission);
        } else {    // 否则在tailPermission之后插入permission
            insert(permission, tailPermission);
        }
    }

    /**
     * 连接权限的邻居节点
     * @param permission 权限实体
     */
    @Override
    public void connectNeighborPermissions(T permission) {
        T behindPermission = getBehindNode(permission);
        if (Objects.nonNull(behindPermission)) { moveForward(behindPermission); }
    }

    /**
     * 修改权限
     * @param permission 权限实体
     */
    @Override
    public void modifyPermission(T permission) {
        T oldPermission = getById(permission.getId());  // 处理权限父节点变化
        if (!Objects.equals(permission.getParentId(), oldPermission.getParentId())) {   // 如果父节点变化，需要尾插法添加权限
            tailInsert(permission); // 尾插法添加权限
            connectNeighborPermissions(oldPermission);  // 处理旧权限的邻居节点连接
        }
        if (!permission.getIsValid() && oldPermission.getIsValid()) {   // 如果权限状态从有效变为无效，需要更新所有后代权限
            List<Integer> descendantIds = getDescendantIds(permission.getId()); // 获取所有后代权限ID
            List<T> descendantPermissions = T.toNotValidPermission(getEntityClass(), descendantIds);    // 将所有后代权限ID转换为无效权限列表
            updateBatchById(descendantPermissions); // 更新所有后代权限
        }
        updateById(permission);
    }

    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    @Override
    public void deletePermission(Integer permissionId) {
        T permission = getById(permissionId);
        List<Integer> descendantIds = getDescendantIds(permissionId); // 获取所有后代权限ID
        descendantIds.add(permissionId);

        removeByIds(descendantIds); // 删除所有后代权限

        connectNeighborPermissions(permission); // 连接权限的邻居节点
    }

    /**
     * 权限上移，需先校验可以上移，即permission不是头权限
     * @param permissionId 权限ID
     */
    @Override
    public void moveUp(Integer permissionId) {
        // 获取权限实体
        T permission = getById(permissionId);
        T frontPermission = getFrontNode(permission);
        // 移动权限
        permission.setOrder(permission.getOrder() - 1);
        frontPermission.setOrder(frontPermission.getOrder() + 1);

        updateBatchById(List.of(permission, frontPermission));
    }

    /**
     * 权限下移，需先校验可以下移，即permission不是尾权限
     * @param permissionId 权限ID
     */
    @Override
    public void moveDown(Integer permissionId) {
        // 获取权限实体
        T permission = getById(permissionId);
        T behindPermission = getBehindNode(permission);
        // 移动权限
        permission.setOrder(permission.getOrder() + 1);
        behindPermission.setOrder(behindPermission.getOrder() - 1);

        this.updateBatchById(List.of(permission, behindPermission));
    }

    /**
     * 校验权限是否为尾权限
     * @param permission 权限实体
     * @return 是否为尾权限
     */
    @Override
    public Boolean isTailPermission(T permission) {
        T tailPermission = getTailNode(permission.getParentId());
        return Objects.equals(permission.getId(), tailPermission.getId());
    }

    /**
     * 保存或更新权限
     * @param permission 权限实体
     * @return 权限ID
     */
    @Override
    public Integer saveOrUpdatePermission(T permission) {
        if (permission.getId() == null) {   // 若权限ID为空，执行保存操作
            save(permission);
        } else {    // 若权限ID不为空，执行更新操作
            updateById(permission);
        }
        return permission.getId();
    }

    /**
     * 将指定权限及其后的所有权限前移指定长度
     * @param permission 权限实体
     * @param length 前移长度，默认值为1
     */
    @Override
    public void moveForward(T permission, Integer... length) {
        int len = (length != null && length.length > 0) ? length[0] : 1;
        // 将permission及其后的所有权限前移指定长度
        update().eq("parent_id", permission.getParentId())
                 .ge("`order`", permission.getOrder())
                 .set("`order`", permission.getOrder() - len)
                 .update();
    }

    /**
     * 将指定权限及其后的所有权限后移指定长度
     * @param permission 权限实体
     * @param length 后移长度，默认值为1
     */
    @Override
    public void moveBackward(T permission, Integer... length) {
        int len = (length != null && length.length > 0) ? length[0] : 1;
        update().eq("parent_id", permission.getParentId())
                 .ge("`order`", permission.getOrder())
                 .set("`order`", permission.getOrder() + len)
                 .update();
    }

    /**
     * 获取指定父节点ID的头节点
     * @param parentId 父节点ID
     * @return 头节点
     */
    private T getHeadNode(Integer parentId) {
        return query().eq("parent_id", parentId)
                .eq("`order`", PermissionConstant.HEAD_PERMISSION_ORDER)
                .one();
    }

    /**
     * 获取指定父节点ID的最后一个子节点
     * @param parentId 父节点ID
     * @return 最后一个子节点
     */
    private T getTailNode(Integer parentId) {
        return query().eq("parent_id", parentId)
                .orderByDesc("`order`")
                .last("limit 1")
                .one();
    }

    /**
     * 获取指定权限的前一个权限
     * @param permission 权限实体
     * @return 前一个权限
     */
    private T getFrontNode(T permission) {
        Integer order = permission.getOrder();
        if (Objects.equals(order, PermissionConstant.HEAD_PERMISSION_ORDER)) { return null; }
        return query().eq("parent_id", permission.getParentId())
                .eq("`order`", order - 1)
                .one();
    }

    /**
     * 获取指定权限的后一个权限
     * @param permission 权限实体
     * @return 后一个权限
     */
    private T getBehindNode(T permission) {
        return query().eq("parent_id", permission.getParentId())
                .eq("`order`", permission.getOrder() + 1)
                .one();
    }

    /**
     * 获取指定父节点ID的所有子节点
     * @param parentId 父节点ID
     * @return 子节点列表
     */
    private List<T> getByParentId(Integer parentId) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);
        return list(wrapper);
    }
}
