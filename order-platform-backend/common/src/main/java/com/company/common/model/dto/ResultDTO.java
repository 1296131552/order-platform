package com.company.common.model.dto;

import com.company.common.enums.IResult;
import com.company.common.enums.result.GlobalResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
/**
 * 通用返回结果
 * @param <T> 数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 私有构造方法
     */
    private ResultDTO() {
    }
    
    /**
     * 构造方法
     * @param code 状态码
     * @param message 返回消息
     * @param data 返回数据
     */
    private ResultDTO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 成功返回结果
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> success() {
        return new ResultDTO<>(GlobalResultCode.SUCCESS.getCode(), GlobalResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功返回结果
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> success(T data) {
        return new ResultDTO<>(GlobalResultCode.SUCCESS.getCode(), GlobalResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResultDTO<T> success(String message) {
        return new ResultDTO<>(GlobalResultCode.SUCCESS.getCode(), message, null);
    }
    
    /**
     * 成功返回结果
     * @param data 返回数据
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> success(T data, String message) {
        return new ResultDTO<>(GlobalResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败返回结果
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> failure() {
        return new ResultDTO<>(GlobalResultCode.ERROR.getCode(), GlobalResultCode.ERROR.getMessage(), null);
    }
    
    /**
     * 失败返回结果
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> failure(String message) {
        return new ResultDTO<>(GlobalResultCode.ERROR.getCode(), message, null);
    }
    
    /**
     * 失败返回结果
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> failure(Integer code, String message) {
        return new ResultDTO<>(code, message, null);
    }
    
    /**
     * 根据枚举返回结果
     * @param result 状态码枚举
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> of(IResult result) {
        return new ResultDTO<>(result.getCode(), result.getMessage(), null);
    }
    
    /**
     * 根据枚举返回结果，并包含数据
     * @param result 状态码枚举
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> ResultDTO<T> of(IResult result, T data) {
        return new ResultDTO<>(result.getCode(), result.getMessage(), data);
    }

}
