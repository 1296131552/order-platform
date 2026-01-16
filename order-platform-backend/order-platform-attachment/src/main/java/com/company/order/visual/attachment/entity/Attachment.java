package com.company.order.visual.attachment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 附件实体
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_attachment")
public class Attachment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（MIME类型）
     */
    private String fileType;

    /**
     * 业务类型（多态关联：order/shipment/partner等）
     */
    private String businessType;

    /**
     * 业务ID（多态关联）
     */
    private Long businessId;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 上传人ID
     */
    private Long uploaderId;

    /**
     * 上传人姓名（冗余）
     */
    private String uploaderName;

    /**
     * 是否删除（0-否 1-是）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
