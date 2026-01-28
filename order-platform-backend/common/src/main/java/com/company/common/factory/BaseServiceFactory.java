package com.company.common.factory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础服务工厂类，用于根据实体类获取对应的服务
 * @param <S> 服务接口类型
 * @param <T> 实体类类型
 */
public abstract class BaseServiceFactory<S, T> {
    
    private Map<Class<?>, S> serviceMap;

    /**
     * 初始化服务映射表，确保在使用前已初始化
     */
    protected void initServiceMap() {
        if (serviceMap == null) {
            serviceMap = new ConcurrentHashMap<>();
        }
    }

    /**
     * 注册服务，将实体类与服务实例关联起来
     * @param entityClass 实体类
     * @param service 服务实例
     */
    protected <E extends T, R extends S> void registerService(Class<E> entityClass, R service) {
        initServiceMap();
        serviceMap.put(entityClass, service);
    }
    
    /**
     * 根据实体类获取对应的服务
     * @param entityClass 实体类
     * @return 对应的服务
     */
    @SuppressWarnings("unchecked")
    public <E extends T, R extends S> R getService(Class<E> entityClass) {
        R service = (R) serviceMap.get(entityClass);
        if (service == null) {
            throw new IllegalArgumentException("不支持的权限实体类型: " + entityClass.getName());
        }
        return service;
    }
    
    /**
     * 通过反射从泛型类中获取指定位置的泛型参数对应的服务
     * @param genericClass 泛型类实例
     * @param typeParameterIndex 泛型参数位置（从0开始）
     * @return 对应的服务
     */
    @SuppressWarnings("unchecked")
    public <E extends T, R extends S> R getServiceByGeneric(Object genericClass, int typeParameterIndex) {
        Class<?> clazz = genericClass.getClass();
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        
        if (typeParameterIndex < 0 || typeParameterIndex >= typeArguments.length) {
            throw new IllegalArgumentException("获取泛型参数" + typeParameterIndex + "失败");
        }
        
        Class<E> entityClass = (Class<E>) typeArguments[typeParameterIndex];
        return getService(entityClass);
    }
}