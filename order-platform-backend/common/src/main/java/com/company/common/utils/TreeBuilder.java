package com.company.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreeBuilder<T, ID> {

    /**
     * 构建节点ID到节点的映射
     * @param nodes 节点列表
     * @param getId 节点ID获取函数
     * @return 节点ID到节点的映射
     */
    private Map<ID, T> buildMap(List<T> nodes, Function<T, ID> getId) {
        return nodes.stream()
                .collect(Collectors.toMap(getId, Function.identity()));
    }

    /**
     * 构建父节点ID到子节点列表的映射
     * @param nodes 节点列表
     * @param getParentId 父节点ID获取函数
     * @return 父节点ID到子节点列表的映射
     */
    private Map<ID, List<T>> buildParentChildrenMap(List<T> nodes, Function<T, ID> getParentId) {
        Map<ID, List<T>> parentChildrenMap = new HashMap<>();
        nodes.forEach(node -> {
            ID parentId = getParentId.apply(node);
            parentChildrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        });
        return parentChildrenMap;
    }

    /**
     * 构建树结构
     * @param nodes 节点列表
     * @param getId 节点ID获取函数
     * @param getParentId 父节点ID获取函数
     * @param getChildren 子节点列表获取函数
     * @param rootParentId 根节点的父节点ID
     * @return 树结构的节点列表
     */
    public List<T> buildTree(List<T> nodes,
                             Function<T, ID> getId,
                             Function<T, ID> getParentId,
                             Function<T, List<T>> getChildren,
                             ID rootParentId,
                             Function<List<T>, List<T>> siblingSorter) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<ID, T> nodeMap = buildMap(nodes, getId);    // 节点ID到节点的映射
        Map<ID, List<T>> parentChildrenMap = buildParentChildrenMap(nodes, getParentId);    // 父节点ID到子节点列表的映射

        // 对每个父节点的子节点列表进行排序
        if (siblingSorter != null) {
            parentChildrenMap.replaceAll((parentId, children) -> siblingSorter.apply(children));
        }

        // 结果树
        List<T> tree = new ArrayList<>();

        // 将排序后的子节点添加到父节点
        parentChildrenMap.forEach((parentId, children) -> {
            T parentNode = nodeMap.get(parentId);
            if (Objects.equals(parentId, rootParentId)) {
                tree.addAll(children);
            } else if (parentNode != null) {
                getChildren.apply(parentNode).addAll(children);
            } else {
                tree.addAll(children);
            }
        });
        return tree;
    }
}
