package com.company.common.enums;
/**
 * 枚举编码接口
 * @param <T> 编码类型
 */
public interface CodeEnum<T> {
     /**
     * 获取枚举编码
     */
    T getCode();
    
    /**
     * 根据编码获取枚举
     * @param enumClass 枚举类
     * @param code 编码
     * @param <E> 枚举类型
     * @param <T> 编码类型
     * @return 枚举实例
     */
    static <E extends Enum<E> & CodeEnum<T>, T> E getByCode(Class<E> enumClass, T code) {
        if (code == null) {
            return null;
        }
        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}