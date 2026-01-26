package com.company.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class TypeUtil {

    public <T> T convertValue(Object value, Class<T> targetType) {
        return convertValue(value, targetType, null);
    }

    /**
     * 通用类型转换方法（支持 List 类型）
     * @param value 要转换的值
     * @param targetType 目标类型
     * @param elementType List 的元素类型（如果 targetType 是 List 的话，可以为 null）
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object value, Class<T> targetType, Class<?> elementType) {
        if (value == null) {
            return null;
        }
        
        // 如果目标类型是 List
        if (List.class.equals(targetType)) {
            return (T) convertToList(value, elementType);
        }
        
        // 处理基本类型
        if (targetType == Boolean.class) {
            return (T) Boolean.valueOf("1".equals(value));
        } else if (targetType == Integer.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (targetType == Double.class) {
            return (T) Double.valueOf(value.toString());
        } else {
            return (T) value.toString();
        }
    }

    /**
     * 将值转换为指定元素类型的 List
     * @param value 要转换的值
     * @param elementType List 元素类型
     * @return 转换后的 List
     */
    private List<?> convertToList(Object value, Class<?> elementType) {
        String stringValue = value.toString();

        // 处理空值或空字符串
        if (stringValue == null || stringValue.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 按逗号分割字符串
        String[] parts = stringValue.split(",");
        List<Object> result = new ArrayList<>();

        for (String part : parts) {
            String trimmedPart = part.trim();
            if (!trimmedPart.isEmpty()) {
                // 根据元素类型转换每个元素
                Object convertedElement = convertValue(trimmedPart, elementType);
                result.add(convertedElement);
            }
        }

        return result;
    }

    /**
     * 将各种类型的值转换为字符串
     * @param value 要转换的值
     * @return 转换后的字符串
     */
    public String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Boolean) {
            // 布尔值转换：true -> "1", false -> "0"
            return ((Boolean) value) ? "1" : "0";
        } else if (value instanceof Number) {
            // 数字类型直接转换为字符串
            return value.toString();
        } else if (value instanceof String) {
            // 字符串直接返回
            return (String) value;
        } else {
            // 其他类型调用toString()方法
            return value.toString();
        }
    }

    /**
     * 将各种类型的值转换为字符串（支持自定义布尔值转换）
     * @param value 要转换的值
     * @param trueValue 布尔true值对应的字符串
     * @param falseValue 布尔false值对应的字符串
     * @return 转换后的字符串
     */
    public String convertToString(Object value, String trueValue, String falseValue) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Boolean) {
            return ((Boolean) value) ? trueValue : falseValue;
        } else {
            return convertToString(value);
        }
    }

    /**
     * 将 List 转换为逗号分隔的字符串
     * @param value List 值
     * @return 逗号分隔的字符串
     */
    private String convertListToString(Object value) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("值不是 List 类型");
        }

        return list.stream()
                .filter(Objects::nonNull)
                .map(this::convertToString)
                .collect(Collectors.joining(","));
    }

    /**
     * 带类型校验的转换为字符串方法
     * @param value 要转换的值
     * @param expectedType 期望的类型
     * @return 转换后的字符串
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    public String convertToStringWithValidation(Object value, Class<?> expectedType) {
        return convertToStringWithValidation(value, expectedType, null);
    }

    /**
     * 通用带类型校验的转换为字符串方法（支持 List）
     * @param value 要转换的值
     * @param expectedType 期望的类型
     * @param elementType List 的元素类型（如果 expectedType 是 List 的话，可以为 null）
     * @return 转换后的字符串
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    public String convertToStringWithValidation(Object value, Class<?> expectedType, Class<?> elementType) {
        if (value == null) {
            return null;
        }
        
        // 如果期望类型是 List
        if (List.class.equals(expectedType)) {
            validateListType(value, elementType);
            return convertListToString(value);
        }
        
        // 处理基本类型
        validateType(value, expectedType);
        return convertToString(value);
    }

    /**
     * 校验值的类型是否匹配期望类型
     * @param value 要校验的值
     * @param expectedType 期望的类型
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateType(Object value, Class<?> expectedType) {
        if (value == null) {
            return;
        }
        
        if (expectedType == Boolean.class) {
            validateBooleanType(value);
        } else if (expectedType == Integer.class) {
            validateIntegerType(value);
        } else if (expectedType == Double.class) {
            validateDoubleType(value);
        } else if (expectedType == String.class) {
            validateStringType(value);
        } else {
            validateOtherType(value, expectedType);
        }
    }

    /**
     * 校验布尔类型
     * @param value 要校验的值
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateBooleanType(Object value) {
        if (!(value instanceof Boolean)) {
            String stringValue = value.toString().toLowerCase();
            if (!"true".equals(stringValue) && !"false".equals(stringValue) && 
                !"1".equals(stringValue) && !"0".equals(stringValue)) {
                throw new IllegalArgumentException("值无法转换为布尔类型: " + value);
            }
        }
    }

    /**
     * 校验整数类型
     * @param value 要校验的值
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateIntegerType(Object value) {
        if (!(value instanceof Integer)) {
            if (value instanceof Number) {
                // 其他数字类型，检查是否为整数
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue != Math.floor(doubleValue)) {
                    throw new IllegalArgumentException("数值不是整数: " + value);
                }
            } else {
                // 尝试解析字符串
                try {
                    Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("值无法转换为整数类型: " + value, e);
                }
            }
        }
    }

    /**
     * 校验双精度浮点数类型
     * @param value 要校验的值
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateDoubleType(Object value) {
        if (!(value instanceof Double)) {
            if (value instanceof Number) {
                // 其他数字类型，直接允许（可以转换为Double）
                // 无需额外校验
            } else {
                // 尝试解析字符串
                try {
                    Double.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("值无法转换为双精度浮点数类型: " + value, e);
                }
            }
        }
    }

    /**
     * 校验字符串类型
     * @param value 要校验的值
     */
    private void validateStringType(Object value) {
        // 字符串类型：任何非null值都可以转换为字符串
        // 无需特殊校验
    }

    /**
     * 校验其他类型
     * @param value 要校验的值
     * @param expectedType 期望的类型
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateOtherType(Object value, Class<?> expectedType) {
        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                String.format("期望类型 %s，但实际类型为 %s", 
                    expectedType.getSimpleName(), value.getClass().getSimpleName()));
        }
    }

    /**
     * 校验 List 类型
     * @param value 要校验的值
     * @param elementType List 元素类型
     * @throws IllegalArgumentException 当类型不匹配时抛出异常
     */
    private void validateListType(Object value, Class<?> elementType) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("期望 List 类型，但实际类型为 " + value.getClass().getSimpleName());
        }

        for (Object element : list) {
            if (element != null) {
                validateType(element, elementType);
            }
        }
    }
}
