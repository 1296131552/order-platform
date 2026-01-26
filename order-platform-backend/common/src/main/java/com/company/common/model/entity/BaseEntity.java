package com.company.common.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class BaseEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;

    /**
      * 更新时间
      */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入时自动填充
    private LocalDateTime updateTime;

    /**
     * 是否被删除(1:是 0:否)
     */
    @TableLogic
    private Boolean isDelete;

    @Serial // 语法合法性检查，避免因命名 / 格式错误导致序列化相关问题
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
