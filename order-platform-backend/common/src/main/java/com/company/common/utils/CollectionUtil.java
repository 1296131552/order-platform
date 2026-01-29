package com.company.common.utils;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CollectionUtil {
    /**
     * 比较两个集合的新增元素
     * @param oldCollection 旧集合
     * @param newCollection 新集合
     * @return 新增元素列表
     */
    public static <T> List<T> AddedCompare(Collection<T> oldCollection, Collection<T> newCollection) {
        Set<T> oldSet = new HashSet<>(oldCollection);
        return newCollection.stream()
                .filter(item -> !oldSet.contains(item))
                .collect(Collectors.toList());
    }

    /**
     * 比较两个集合的删除元素
     * @param oldCollection 旧集合
     * @param newCollection 新集合
     * @return 删除元素列表
     */
    public static <T> List<T> RemovedCompare(Collection<T> oldCollection, Collection<T> newCollection) {
        Set<T> newSet = new HashSet<>(newCollection);
        return oldCollection.stream()
                .filter(item -> !newSet.contains(item))
                .collect(Collectors.toList());
    }

    /**
     * 比较两个集合的公共元素
     * @param oldCollection 旧集合
     * @param newCollection 新集合
     * @return 公共元素列表
     */
    public static <T> List<T> CommonCompare(Collection<T> oldCollection, Collection<T> newCollection) {
        Set<T> oldSet = new HashSet<>(oldCollection);
        return newCollection.stream()
                .filter(oldSet::contains)
                .collect(Collectors.toList());
    }
}
